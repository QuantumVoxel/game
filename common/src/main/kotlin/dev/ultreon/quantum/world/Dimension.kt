@file:Suppress("LeakingThis")

package dev.ultreon.quantum.world

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.math.BoundingBoxD
import dev.ultreon.quantum.scripting.*
import dev.ultreon.quantum.scripting.function.VirtualFunction
import dev.ultreon.quantum.scripting.function.function
import dev.ultreon.quantum.util.BlockHit
import dev.ultreon.quantum.util.RayD
import dev.ultreon.quantum.util.WorldRayCaster
import dev.ultreon.quantum.vec3d
import ktx.collections.GdxArray
import kotlin.math.floor

const val SIZE = 16

@JvmInline
value class BlockFlags(val value: Int) {
  fun has(flag: Int): Boolean = value and flag == flag

  operator fun plus(flag: Int): BlockFlags = BlockFlags(value or flag)
  operator fun plus(flag: BlockFlags): BlockFlags = BlockFlags(value or flag.value)

  companion object {
    val NONE = BlockFlags(0)
    val REPLACE = BlockFlags(1)
    val UPDATE = BlockFlags(2)
    val SYNC = BlockFlags(4)
  }
}

abstract class Dimension : Disposable, ContextAware<Dimension> {
  val entityManager: EntityManager = EntityManager(this)

  abstract operator fun get(x: Int, y: Int, z: Int): Block
  operator fun set(x: Int, y: Int, z: Int, block: Block) =
    set(x, y, z, block, BlockFlags.SYNC + BlockFlags.UPDATE)

  abstract fun set(x: Int, y: Int, z: Int, block: Block, flags: BlockFlags)

  override fun dispose() = Unit

  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "get_block" -> ContextValue(ContextType.function, function(
        ContextParam("x", ContextType.int),
        ContextParam("y", ContextType.int),
        ContextParam("z", ContextType.int),
        function = {
          return@function ContextValue(
            ContextType.block,
            this[it.getInt("x") ?: 0, it.getInt("y") ?: 0, it.getInt("z") ?: 0]
          )
        }
      ))
      "set_block" -> ContextValue(ContextType.function, function(
        ContextParam("x", ContextType.int),
        ContextParam("y", ContextType.int),
        ContextParam("z", ContextType.int),
        ContextParam("block", ContextType.block),
        function = {
          this[it.getInt("x") ?: 0, it.getInt("y") ?: 0, it.getInt("z") ?: 0] = it.get<Block>("block") ?: Blocks.air
          return@function ContextValue(
            ContextType.block,
            this[it.getInt("x") ?: 0, it.getInt("y") ?: 0, it.getInt("z") ?: 0]
          )
        }
      ))
      else -> null
    }
  }

  fun collide(box: BoundingBoxD, collideFluid: Boolean): List<BoundingBoxD> {
    val boxes: MutableList<BoundingBoxD> = ArrayList()
    val xMin = floor(box.min.x).toInt()
    val xMax = floor(box.max.x).toInt()
    val yMin = floor(box.min.y).toInt()
    val yMax = floor(box.max.y).toInt()
    val zMin = floor(box.min.z).toInt()
    val zMax = floor(box.max.z).toInt()

    for (x in xMin..xMax) {
      for (y in yMin..yMax) {
        for (z in zMin..zMax) {
          checkCollide(x, y, z, collideFluid, box, boxes)
        }
      }
    }

    return boxes
  }

  private fun checkCollide(
    x: Int,
    y: Int,
    z: Int,
    collideFluid: Boolean,
    box: BoundingBoxD,
    boxes: MutableList<BoundingBoxD>,
  ) {
    val block: Block = this[x, y, z]
    if (block.hasCollider && (!collideFluid || block.isFluid)) {
      val blockBox: GdxArray<BoundingBoxD> = block.boundsAt(vec3d(x, y, z))
      for (i in 0 until blockBox.size) {
        val b = blockBox[i]
        if (b.intersects(box)) {
          boxes.add(b)
        }
      }
    }
  }

  fun tick() {

  }

  override fun supportedTypes(): List<ContextType<*>> {
    return listOf(ContextType.dimension)
  }

  override fun contextType(): ContextType<Dimension> {
    return ContextType.dimension
  }

  abstract fun chunkAt(x: Int, y: Int, z: Int): Chunk?

  open fun chunkAtBlock(x: Int, y: Int, z: Int): Chunk? {
    return chunkAt(x.floorDiv(SIZE), y.floorDiv(SIZE), z.floorDiv(SIZE))
  }

  fun rayTrace(ray: RayD): BlockHit {
    return WorldRayCaster.rayCast(BlockHit(ray), this)
  }

  override val persistentData: PersistentData = PersistentData()
}
