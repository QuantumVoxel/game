@file:Suppress("LeakingThis")

package dev.ultreon.quantum.world

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.math.BoundingBoxD
import dev.ultreon.quantum.scripting.*
import dev.ultreon.quantum.scripting.function.function
import dev.ultreon.quantum.util.BlockHit
import dev.ultreon.quantum.util.RayD
import dev.ultreon.quantum.util.WorldRayCaster
import dev.ultreon.quantum.vec3d
import ktx.collections.GdxArray
import kotlin.math.floor

/**
 * The size of a chunk.
 * Generally this number should be a power of 2, such as 16.
 *
 * Default: `16`
 */
const val SIZE = 16

/**
 * A flag that can be used to modify the way a block is updated / set.
 */
@JvmInline
value class BlockFlags(val value: Int) {
  /**
   * Check if this flag is set.
   */
  operator fun contains(flag: Int): Boolean = value and flag == flag

  operator fun plus(flag: Int): BlockFlags = BlockFlags(value or flag)
  operator fun plus(flag: BlockFlags): BlockFlags = BlockFlags(value or flag.value)

  companion object {
    /**
     * No flags
     */
    val NONE = BlockFlags(0)

    /**
     * Replacing a block with a new block.
     */
    val REPLACE = BlockFlags(1)

    /**
     * Updating a block, and causing chain reactions if applicable.
     */
    val UPDATE = BlockFlags(2)

    /**
     * Syncing a block to the client (if on the server).
     *
     * NOTE: This flag is only used on the server.
     */
    val SYNC = BlockFlags(4)
  }
}

/**
 * A dimension of the world that contains blocks.
 * This is the base class for all dimensions.
 * Subclasses should implement the [get] and [set] methods.
 * Server-side implementations recommend having sparse memory usage.
 * Such as palette storage.
 *
 * @see Block
 * @see Blocks
 * @see EntityManager
 *
 * @property entityManager The entity manager of the dimension.
 */
abstract class Dimension : Disposable, ContextAware<Dimension> {
  val entityManager: EntityManager = EntityManager(this)

  /**
   * Gets the block at the specified position.
   *
   * ### Example usage:
   * ```kotlin
   * val block = dimension[0, 0, 0]
   * ```
   *
   * @param x The x-coordinate of the block.
   * @param y The y-coordinate of the block.
   * @param z The z-coordinate of the block.
   * @return The block at the specified position.
   */
  abstract operator fun get(x: Int, y: Int, z: Int): Block

  /**
   * Sets the block at the specified position.
   *
   * ### Example usage:
   * ```kotlin
   * dimension[0, 0, 0] = Blocks.AIR
   * ```
   *
   * @see Blocks
   * @param x The x-coordinate of the block.
   * @param y The y-coordinate of the block.
   * @param z The z-coordinate of the block.
   * @param block The block to set at the specified position.
   */
  operator fun set(x: Int, y: Int, z: Int, block: Block) =
    set(x, y, z, block, BlockFlags.SYNC + BlockFlags.UPDATE)

  /**
   * Sets the block at the specified position.
   *
   * ### Example usage:
   * ```kotlin
   * dimension[0, 0, 0, BlockFlags.UPDATE] = Blocks.AIR
   * ```
   *
   * @see Blocks
   * @see BlockFlags.UPDATE
   * @param x The x-coordinate of the block.
   * @param y The y-coordinate of the block.
   * @param z The z-coordinate of the block.
   * @param block The block to set at the specified position.
   * @param flags The flags to set at the specified position.
   */
  abstract fun set(x: Int, y: Int, z: Int, block: Block, flags: BlockFlags)

  override fun dispose() = Unit

  /**
   * Gets a field of the dimension.
   *
   * @param name The name of the field.
   * @param contextJson The context JSON.
   * @return The field value.
   */
  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "get_block" -> ContextValue(
        ContextType.function, function(
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

      "set_block" -> ContextValue(
        ContextType.function, function(
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

  /**
   * Collides the given bounding box with the blocks in the dimension.
   *
   * @param box The bounding box.
   * @param collideFluid Whether to collide with fluid blocks.
   * @return A list of bounding boxes that are intersected by the given bounding box.
   */
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

  /**
   * Checks if the block at the specified position collides with the given bounding box.
   *
   * @param x The x-coordinate of the block.
   * @param y The y-coordinate of the block.
   * @param z The z-coordinate of the block.
   * @param collideFluid Whether to collide with fluid blocks.
   * @param box The bounding box.
   * @param boxes The list of bounding boxes that are intersected by the given bounding box.
   */
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

  /**
   * Ticks the dimension.
   */
  open fun tick() {

  }

  override fun supportedTypes(): List<ContextType<*>> {
    return listOf(ContextType.dimension)
  }

  override fun contextType(): ContextType<Dimension> {
    return ContextType.dimension
  }

  /**
   * Gets the chunk at the specified position.
   *
   * @param x The x-coordinate of the chunk.
   * @param y The y-coordinate of the chunk.
   * @param z The z-coordinate of the chunk.
   * @return The chunk at the specified position.
   */
  abstract fun chunkAt(x: Int, y: Int, z: Int): Chunk?

  /**
   * Gets the chunk at the specified block position.
   *
   * @param x The x-coordinate of the block.
   * @param y The y-coordinate of the block.
   * @param z The z-coordinate of the block.
   * @return The chunk at the specified block position.
   */
  open fun chunkAtBlock(x: Int, y: Int, z: Int): Chunk? {
    return chunkAt(x.floorDiv(SIZE), y.floorDiv(SIZE), z.floorDiv(SIZE))
  }

  /**
   * Casts a ray in the world and returns the block hit.
   *
   * @param ray The ray to cast.
   * @return The block hit by the ray.
   */
  fun rayTrace(ray: RayD): BlockHit {
    return WorldRayCaster.rayCast(BlockHit(ray), this)
  }

  /**
   * The persistent data of the dimension.
   */
  override val persistentData: PersistentData = PersistentData()
}
