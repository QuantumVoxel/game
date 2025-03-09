@file:Suppress("UnusedImport")

package dev.ultreon.quantum.client.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.GridPoint3
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.util.BlockHit
import dev.ultreon.quantum.util.RayD
import dev.ultreon.quantum.world.BlockFlags
import dev.ultreon.quantum.world.Dimension
import dev.ultreon.quantum.world.SIZE
import ktx.collections.GdxSet
import ktx.collections.gdxSetOf
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.Pair
import kotlin.Unit
import kotlin.also
import kotlin.apply
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.arrayListOf
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.sortBy
import kotlin.collections.toList
import kotlin.floorDiv
import kotlin.let
import kotlin.mod
import kotlin.to

val renderDistance: Int
  get() = 8

/**
 * Represents a client-side dimension.
 * @param material The material to use for the chunks.
 * @constructor Creates a new client-side dimension.
 */
open class ClientDimension(private val material: Material) : Dimension() {
  private lateinit var player: LocalPlayer
  val chunks: MutableMap<Long, ClientChunk> = ConcurrentHashMap()
  val chunksToLoad = PriorityBlockingQueue<ChunkPos>()
  val generator = Generator()
  val asyncChunkGen = com.badlogic.gdx.utils.async.AsyncExecutor(8, "ChunkGeneratorPool")
  internal var toRemove = gdxSetOf<GridPoint3>()

  internal var toRebuild = gdxSetOf<GridPoint3>()
  private var time = 0f

  private val environment: Environment = Environment().apply {
//    add(sunLight)
//    set(createAmbientLight(0.4f, 0.4f, 0.4f, 1f))
//
//    shadowMap = sunLight
  }

//  private val shadowBatch = ModelBatch(
//    if (gamePlatform.isWebGL3 || gamePlatform.isGL30 || gamePlatform.isGLES3) {
//      object : DepthShaderProvider(
//        (quantum.clientResources require NamespaceID.of(path = "shaders/programs/depth.vsh")).text,
//        (quantum.clientResources require NamespaceID.of(path = "shaders/programs/depth.fsh")).text
//      ) {
//        override fun createShader(renderable: Renderable): Shader {
//          return DepthShader(
//            renderable,
//            this.config,
//            "#version 300 es\n\n" + DefaultShader.createPrefix(renderable, config)
//          )
//        }
//      }
//    } else {
//      object : DepthShaderProvider(
//        (quantum.clientResources require NamespaceID.of(path = "shaders/programs/legacy/depth.vsh")).text,
//        (quantum.clientResources require NamespaceID.of(path = "shaders/programs/legacy/depth.fsh")).text
//      ) {
//
//      }
//    },
//    DefaultRenderableSorter()
//  )

  override fun get(x: Int, y: Int, z: Int): Block {
    return chunks[location(x.floorDiv(SIZE), y.floorDiv(SIZE), z.floorDiv(SIZE))]
      ?.get(x.mod(SIZE), y.mod(SIZE), z.mod(SIZE)) ?: Blocks.air
  }

  override fun set(x: Int, y: Int, z: Int, block: Block, flags: BlockFlags) {
    val get = chunks[location(x.floorDiv(SIZE), y.floorDiv(SIZE), z.floorDiv(SIZE))]
    get?.let {
      logger.info("Setting blocks at $x, $y, $z to $block")
      it.set(x % SIZE, y % SIZE, z % SIZE, block, flags)
      it.rebuild()
      forChunksAround(it) { rebuild() }
    }
  }

  fun location(x: Int, y: Int, z: Int): Long {
    return (x.toLong() and 0xFFFFFF) or
      ((y.toLong() and 0xFFFFFF) shl 24) or
      ((z.toLong() and 0xFFFFFF) shl 48)
  }

  override fun chunkAt(x: Int, y: Int, z: Int): ClientChunk? {
    return chunks[location(x, y, z)]
  }

  override fun chunkAtBlock(x: Int, y: Int, z: Int): ClientChunk? {
    return chunkAt(x.floorDiv(SIZE), y.floorDiv(SIZE), z.floorDiv(SIZE))
  }

  fun put(chunk: ClientChunk): Boolean {
    val location = location(chunk.chunkPos.x, chunk.chunkPos.y, chunk.chunkPos.z)
    val oldChunk = chunks[location]
    if (oldChunk != null) {
      if (remove(oldChunk.chunkPos)) {
        if (chunk.loading) {
          return true
        }
        chunk.disposeChunk()
        return true
      }
      logger.warn("Overridden chunk at ${chunk.chunkPos}")
    }
    chunks[location] = chunk
    return false
  }

  fun putAsync(chunk: ClientChunk): Boolean {
    return QuantumVoxel {
      put(chunk)
    }
  }

  fun remove(pos: GridPoint3): Boolean {
    val chunk = chunks[location(pos.x, pos.y, pos.z)] ?: return false
    if (!chunk.disposeChunk()) {
      return true
    }
    if (chunks.remove(location(chunk.chunkPos.x, chunk.chunkPos.y, chunk.chunkPos.z)) == null) {
      logger.warn("Tried to remove nonexistent chunk at ${chunk.chunkPos}")
    }

    forChunksAround(chunk) { rebuild() }
    return false
  }

  fun loadChunk(cx: Int, cy: Int, cz: Int, build: Boolean = true) {
    val chunk = ClientChunk(cx, cy, cz, material, this)
    if (put(chunk.apply {
        generate(this)
      })) return
    chunk.apply {
      if (build) {
        rebuild()
      }
    }
  }

  fun loadChunkAsync(cx: Int, cy: Int, cz: Int, build: Boolean = true) {
    val chunk = ClientChunk(cx, cy, cz, material, this)
    if (putAsync(chunk.also { return@also asyncChunkGen.submit { generateAsync(it) }.get() })) {
      return
    }
    chunk.buildModel()
    forChunksAround(chunk) { rebuild() }
  }

  inline fun forChunksAround(chunk: ClientChunk, crossinline action: ClientChunk.() -> Unit) {
    val position = chunk.chunkPos
    var didAction = false
    for (x in (position.x - 1)..(position.x + 1)) {
      for (y in (position.y - 1)..(position.y + 1)) {
        for (z in (position.z - 1)..(position.z + 1)) {
          if (x == position.x && y == position.y && z == position.z) {
            continue
          }

          chunks[location(x, y, z)]?.let(action)?.let {
            didAction = true
          }
        }
      }
    }

    if (!didAction) {
      logger.warn("Didn't find any loaded chunks around ${chunk.chunkPos}")
    }
  }

  val ClientChunk.hasChunksAround: Boolean
    get() {
      for (x in -1..1) for (y in -1..1) for (z in -1..1) {
        if (x == 0 && y == 0 && z == 0) {
          continue
        } else {
          chunks[location(chunkPos.x + x, chunkPos.y + y, chunkPos.z + z)] ?: return false
        }
      }
      return true
    }

  fun rebuildAll() {
    for (chunk in chunks.values) {
      chunk.rebuild(blocking = true)
      Thread.yield()
    }
  }

  fun refreshChunks(position: Vector3D) {
    chunksToLoad.clear()

    val requiredChunks: MutableList<Pair<GridPoint3, Long>> = arrayListOf()
    val cx = position.x.toInt().floorDiv(SIZE)
    val cy = position.y.toInt().floorDiv(SIZE)
    val cz = position.z.toInt().floorDiv(SIZE)
    for (x in -renderDistance..renderDistance) {
      for (y in -renderDistance..renderDistance) {
        for (z in -renderDistance..renderDistance) {
          val dcy = y + cy
          val dcx = x + cx
          val dcz = z + cz

          val chunk = chunks[location(dcx, dcy, dcz)]
          if (chunk == null) {
            requiredChunks.add(GridPoint3(dcx, dcy, dcz) to location(dcx, dcy, dcz))
          }

          Thread.yield()
        }
      }
    }

    requiredChunks.sortBy {
      it.first.dst(cx, cy, cz)
    }

    logger.debug("Refreshing chunks: ${requiredChunks.size}")

    val toRemove = GdxSet<GridPoint3>()
    val toRebuild = GdxSet<GridPoint3>()
    val await = chunks.map { it.value }
    for (chunk in await) {
      if (chunk.chunkPos.dst(cx, cy, cz) > renderDistance) {
        toRemove.add(chunk.chunkPos)
        toRebuild.remove(chunk.chunkPos)
        forChunksAround(chunk) { toRebuild.add(chunk.chunkPos) }
      }

      Thread.yield()
    }

    this@ClientDimension.toRemove = toRemove
    this@ClientDimension.toRebuild = toRebuild

    for ((pos, index) in requiredChunks) {
      if (pos.dst(cx, cy, cz) > renderDistance) continue
      if (chunks[index] != null) continue

      val element = ChunkPos(pos)
      if (chunksToLoad.contains(element)) continue // Yea let's not nuke the queue

      this.chunksToLoad.add(element)
      Thread.yield()
    }

    for (chunk in toRemove.toList()) {
      QuantumVoxel.invoke {
        remove(chunk)
      }
      Thread.yield()
    }
  }

  fun pollChunks() {
    val toRemove = GdxSet(toRemove)
    this.toRemove.clear()
    for (removing in toRemove) {
      remove(removing)
    }

    val toRebuild = GdxSet(toRebuild)
    this.toRebuild.clear()
    for (rebuilding in toRebuild) {
      val chunk = chunks[location(rebuilding.x, rebuilding.y, rebuilding.z)]
      chunk?.rebuild()
    }
  }

  private fun generate(chunk: ClientChunk) {
    generator.generate(chunk)
  }

  private fun generateAsync(chunk: ClientChunk) {
    generator.generateAsync(chunk)
  }

  override fun tick() {
    super.tick()

    val poll = this.chunksToLoad.poll()
    if (poll != null) {
      val pos = poll.point
      loadChunkAsync(pos.x, pos.y, pos.z, build = true)
    }
  }

  fun render(modelBatch: ModelBatch, camera: PerspectiveCamera) {
//    sunLight.direction.set(0F, 0F, -1F).rotate(-time * 360 / 1000, 1F, 0F, 0F).rotate(40F, 0F, 0F, 1F)
//    sunLight.color.set(1F, 1F, 1F, 1F)
//    sunLight.begin(Vector3.Zero, camera.direction)
//
//    shadowBatch.begin(sunLight.camera)
//
//    for (chunk: ClientChunk in chunks.values) {
//      chunk.reposition(player.positionComponent.position)
//      shadowBatch.render(chunk)
//    }
//
//    shadowBatch.end()
//    sunLight.end()

    val removals = GdxSet<ClientChunk>()

    for (chunk: ClientChunk in chunks.values) {
      chunk.reposition(player.positionComponent.position)
      if (chunk.pendingDispose) {
        removals.add(chunk)
        continue
      }
      modelBatch.render(chunk/*, environment*/)
    }

    for (removing in removals) {
      remove(removing)
    }

    modelBatch.flush()

    time += Gdx.graphics.deltaTime
  }

  private fun remove(pos: ClientChunk) {
    val location = location(pos.chunkPos.x, pos.chunkPos.y, pos.chunkPos.z)
    val chunk = chunks[location]
    chunk?.disposeChunk()
    chunks.remove(location, chunk ?: return)
  }

  override fun dispose() {
    asyncChunkGen.dispose()

    logger.debug("Disposing chunks...")

    for (chunk in chunks.values) {
      val disposeChunk = chunk.disposeChunk()
      if (!disposeChunk) {
        logger.warn("Failed to dispose chunk at ${chunk.chunkPos}")
      }
    }
    chunks.clear()
  }

  fun updateLocations(position: Vector3D) {
    for (chunk in chunks.values) {
      chunk.reposition(position)
    }
  }

  fun rayCast(position: Vector3D, lookVec: Vector3D): BlockHit {
    return rayTrace(RayD(position, lookVec))
  }

  fun spawnPlayer(vec3d: Vector3D): LocalPlayer {
    this.player = LocalPlayer("local", this, vec3d)
    return player
  }
}

private operator fun GridPoint3.component1(): Int = x
private operator fun GridPoint3.component2(): Int = y
private operator fun GridPoint3.component3(): Int = z
