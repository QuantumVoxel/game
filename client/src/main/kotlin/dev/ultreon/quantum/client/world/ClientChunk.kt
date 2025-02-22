package dev.ultreon.quantum.client.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.GridPoint3
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.utils.Pool
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.client.model.FaceCull
import dev.ultreon.quantum.client.model.ModelRegistry
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.client.*
import dev.ultreon.quantum.client.model.ModelBakery
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.util.id
import dev.ultreon.quantum.vec3d
import dev.ultreon.quantum.world.BlockFlags
import dev.ultreon.quantum.world.Chunk
import dev.ultreon.quantum.world.SIZE
import ktx.assets.disposeSafely
import ktx.collections.GdxArray
import ktx.math.vec3
import net.mgsx.gltf.scene3d.attributes.MirrorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import net.mgsx.gltf.scene3d.scene.Scene

var allLoading = 0
  private set

private val lastLoad: Long get() = System.currentTimeMillis()

class ClientChunk(x: Int, y: Int, z: Int, private val material: Material, val dimension: ClientDimension) : Chunk(),
  RenderableProvider {
  private val modelBuilder = ModelBuilder()
  private val _boundingBox: BoundingBox = BoundingBox()
  val boundingBox: BoundingBox
    get() {
      _boundingBox.min.set(renderPosition)
      _boundingBox.max.set(renderPosition).add(SIZE.toFloat(), SIZE.toFloat(), SIZE.toFloat())
      return _boundingBox
    }

  val waterMaterial: Material = Material().also {
    it.set(PBRColorAttribute.createBaseColorFactor(Color.WHITE))
    it.set(PBRTextureAttribute.createBaseColorTexture(textureManager[id(path = "textures/block/water.png")]))
    it.set(PBRTextureAttribute.createMetallicRoughnessTexture(textureManager[id(path = "textures/block/water.mr.png")]))
    it.set(PBRTextureAttribute.createNormalTexture(textureManager[id(path = "textures/block/water.normal.png")]))
    it.set(MirrorAttribute.createSpecular())
  }

  val renderPosition: Vector3 = vec3()
  internal var loading: Boolean = true
  val start: GridPoint3
    get() = chunkPos.cpy().also {
      it.x *= SIZE
      it.y *= SIZE
      it.z *= SIZE
    }
  private var hasBlocks: Boolean = false
  private var airBlocks: Int = SIZE * SIZE * SIZE
  private var dirty: Boolean = true

  val chunkPos: GridPoint3 = GridPoint3(x, y, z)
  val blocks = Array(SIZE) { Array(SIZE) { Array(SIZE) { Blocks.air } } }
  private var worldModel: Model? = null
  var chunkScene: Scene? = null

  override val offset: Vector3D
    get() = vec3d(chunkPos.x * SIZE, chunkPos.y * SIZE, chunkPos.z * SIZE)

  override fun get(x: Int, y: Int, z: Int): Block {
    return blocks[x][y][z]
  }

  override fun set(x: Int, y: Int, z: Int, block: Block) {
    set(x, y, z, block, BlockFlags.SYNC)
  }

  override fun isDisposed(): Boolean {
    return chunkScene == null
  }

  override fun set(x: Int, y: Int, z: Int, block: Block, flags: BlockFlags) {
    val nx = (x % SIZE + SIZE) % SIZE
    val ny = (y % SIZE + SIZE) % SIZE
    val nz = (z % SIZE + SIZE) % SIZE
    val block1 = blocks[nx][ny][nz]
    if (block1 == Blocks.air) {
      airBlocks--
    } else {
      airBlocks++
    }
    blocks[nx][ny][nz] = block
    if (block == Blocks.air) {
      airBlocks++
    } else {
      airBlocks--
    }

    hasBlocks = airBlocks < SIZE * SIZE * SIZE
  }

  fun fillUpTo(y: Int, block: Block, flags: BlockFlags) {
    for (x in 0 until SIZE) {
      for (z in 0 until SIZE) {
        for (i in y downTo 0) {
          if (get(x, i, z) == Blocks.air) {
            set(x, i, z, block, flags)
          }
        }
      }
    }
  }

  fun rebuild(blocking: Boolean = false) {
    dirty = false
    loading = true

    if (blocking) {
      buildModel().get()
    } else {
      buildModel()
    }
  }

  fun rebuildAsync() {
    dirty = false
    loading = true

    buildModel()
  }

  fun buildModel(): Future<Unit> {
    return Future.runAsync {
      val builder = ModelBuilder()
      builder.begin()

      allLoading++
      if (allLoading > 1000 || dimension.chunks.size + allLoading > 8000) {
        throw ProtectionFault("Too many chunks loading")
      }

      QuantumVoxel {
        val bakery = ModelBakery(builder)

        for (x in 0..<SIZE) {
          for (y in 0..<SIZE) {
            for (z in 0..<SIZE) {
              val block = getSafe(x, y, z)
              loadBlockInto(bakery, x, y, z, block, RenderInfo[block.renderType])
            }
          }
        }
      }

      QuantumVoxel {
        // Hotswap model and model instance
        if (chunkScene != null || worldModel != null) {
          if (chunkScene?.lights != null) dimension.sceneManager.removeScene(chunkScene)
          worldModel.disposeSafely()
          worldModel = null
          chunkScene = null
        }

        val model = builder.end()
        worldModel = model
        chunkScene = Scene(worldModel)
        loading = false
        allLoading--
        dimension.sceneManager.addScene(chunkScene)

      }
    }.apply {
      onFailure = {
        logger.error("Failed to build chunk", it.stackTraceToString())
      }
    }
  }

  private fun loadBlockInto(
    bakery: ModelBakery,
    x: Int,
    y: Int,
    z: Int,
    block: Block,
    info: RenderInfo = RenderInfo.default,
  ) {
    if (block != Blocks.air) {
      val model = ModelRegistry[block]
      if (info.name != block.renderType) {
        return
      }
      model.loadInto(
        bakery, x, y, z, FaceCull(
          back = getSafe(x, y, z + 1).let { it != Blocks.air && it.renderType == block.renderType },
          front = getSafe(x, y, z - 1).let { it != Blocks.air && it.renderType == block.renderType },
          left = getSafe(x - 1, y, z).let { it != Blocks.air && it.renderType == block.renderType },
          right = getSafe(x + 1, y, z).let { it != Blocks.air && it.renderType == block.renderType },
          top = getSafe(x, y + 1, z).let { it != Blocks.air && it.renderType == block.renderType },
          bottom = getSafe(x, y - 1, z).let { it != Blocks.air && it.renderType == block.renderType }
        ), AOArray.calculate(this, x, y, z),
        info, ModelInfo(block.element)
      )

      this.hasBlocks = true
    }
  }

  fun getSafe(localX: Int, localY: Int, localZ: Int): Block {
    if (localX < 0 || localX >= SIZE || localY < 0 || localY >= SIZE || localZ < 0 || localZ >= SIZE) {
      val wx = chunkPos.x * SIZE + localX
      val wy = chunkPos.y * SIZE + localY
      val wz = chunkPos.z * SIZE + localZ
      return dimension[wx, wy, wz]
    }
    return this[localX, localY, localZ]
  }

  override fun getRenderables(array: GdxArray<Renderable>, pool: Pool<Renderable>) {
    if (!hasBlocks) return
    chunkScene?.getRenderables(array, pool)
  }

  fun disposeChunk(): Boolean {
    if (!quantum.isOnRenderThread) throw ProtectionFault("Wrong thread")
    if (loading) {
      return false
    }

    if (chunkScene != null) dimension.sceneManager.removeScene(chunkScene)
    chunkScene = null
    worldModel.disposeSafely()
    return true
  }

  override fun dispose() {
    throw UnsupportedOperationException()
  }

  fun reposition(position: Vector3D) {
    chunkScene?.relative(
      position.cpy()
        .sub(this.chunkPos.x * SIZE.toFloat(), this.chunkPos.y * SIZE.toFloat(), this.chunkPos.z * SIZE.toFloat())
    )

    chunkScene?.modelInstance?.transform?.getTranslation(renderPosition)
  }

  fun markDirty() {
    this.dirty = true
  }

  val isDirty: Boolean
    get() = this.dirty
}
