package dev.ultreon.quantum.server

import dev.ultreon.quantum.world.SIZE

/**
 * Represents a 3D index.
 *
 * @param index The index.
 * @property index The index.
 * @property x The x coordinate.
 * @property y The y coordinate.
 * @property z The z coordinate.
 *
 * @constructor Creates a new 3D index.
 */
@JvmInline
value class Index3(val index: Int) {
  /**
   * Creates a new 3D index.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   */
  constructor(x: Int, y: Int, z: Int) : this((x * SIZE + y) * SIZE + z)

  val x get() = index / (SIZE * SIZE) % SIZE
  val y get() = index % (SIZE * SIZE) / SIZE
  val z get() = index % SIZE

  /**
   * This uses the format of [x, y, z].
   */
  override fun toString(): String {
    return "[$x, $y, $z]"
  }
}

/**
 * Represents a 2D index.
 *
 * @param index The index.
 * @property index The index.
 * @property x The x coordinate.
 * @property y The y coordinate.
 *
 * @constructor Creates a new 2D index.
 */
@JvmInline
value class Index2(val index: Int) {
  /**
   * Creates a new 2D index.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  constructor(x: Int, y: Int) : this(x * SIZE + y)

  val x get() = index / SIZE % SIZE
  val y get() = index % SIZE

  /**
   * This uses the format of [x, y].
   */
  override fun toString(): String {
    return "[$x, $y]"
  }
}
