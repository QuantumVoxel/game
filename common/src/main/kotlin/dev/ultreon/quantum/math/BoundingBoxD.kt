package dev.ultreon.quantum.math

/**
 * Encapsulates an axis aligned bounding box represented by a minimum and a maximum Vector. Additionally you can query for the
 * bounding box's center, dimensions and corner points.
 *
 * @author badlogicgames@gmail.com, Xoppa
 */
@Suppress("SpellCheckingInspection")
class BoundingBoxD {
  /**
   * Minimum vector. All XYZ components should be inferior to corresponding [.max] components. Call [.update] if
   * you manually change this vector.
   */
  val min: Vector3D = Vector3D()

  /**
   * Maximum vector. All XYZ components should be superior to corresponding [.min] components. Call [.update] if
   * you manually change this vector.
   */
  val max: Vector3D = Vector3D()

  private val cnt: Vector3D = Vector3D()
  private val dim: Vector3D = Vector3D()

  // NOTE: Added 6-parameter constructor for use in Block#getBoundingBox - by Qubilux (Ultreon)
  constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) : this(
    Vector3D(
      minX,
      minY,
      minZ
    ), Vector3D(maxX, maxY, maxZ)
  )

  /**
   * @param out The [Vector3D] to receive the center of the bounding box.
   * @return The vector specified with the out argument.
   */
  fun getCenter(out: Vector3D): Vector3D {
    return out.set(this.cnt)
  }

  val centerX: Double
    get() = cnt.x

  val centerY: Double
    get() = cnt.y

  val centerZ: Double
    get() = cnt.z

  fun getCorner000(out: Vector3D): Vector3D {
    return out.set(min.x, min.y, min.z)
  }

  fun getCorner001(out: Vector3D): Vector3D {
    return out.set(min.x, min.y, max.z)
  }

  fun getCorner010(out: Vector3D): Vector3D {
    return out.set(min.x, max.y, min.z)
  }

  fun getCorner011(out: Vector3D): Vector3D {
    return out.set(min.x, max.y, max.z)
  }

  fun getCorner100(out: Vector3D): Vector3D {
    return out.set(max.x, min.y, min.z)
  }

  fun getCorner101(out: Vector3D): Vector3D {
    return out.set(max.x, min.y, max.z)
  }

  fun getCorner110(out: Vector3D): Vector3D {
    return out.set(max.x, max.y, min.z)
  }

  fun getCorner111(out: Vector3D): Vector3D {
    return out.set(max.x, max.y, max.z)
  }

  /**
   * @param out The [Vector3D] to receive the dimensions of this bounding box on all three axis.
   * @return The vector specified with the out argument
   */
  fun getDimensions(out: Vector3D): Vector3D {
    return out.set(this.dim)
  }

  val width: Double
    get() = dim.x

  val height: Double
    get() = dim.y

  val depth: Double
    get() = dim.z

  /**
   * @param out The [Vector3D] to receive the minimum values.
   * @return The vector specified with the out argument
   */
  fun getMin(out: Vector3D): Vector3D {
    return out.set(this.min)
  }

  /**
   * @param out The [Vector3D] to receive the maximum values.
   * @return The vector specified with the out argument
   */
  fun getMax(out: Vector3D): Vector3D {
    return out.set(this.max)
  }

  /**
   * Constructs a new bounding box with the minimum and maximum vector set to zeros.
   */
  constructor() {
    this.clr()
  }

  /**
   * Constructs a new bounding box from the given bounding box.
   *
   * @param bounds The bounding box to copy
   */
  constructor(bounds: BoundingBoxD) {
    this.set(bounds)
  }

  /**
   * Constructs the new bounding box using the given minimum and maximum vector.
   *
   * @param minimum The minimum vector
   * @param maximum The maximum vector
   */
  constructor(minimum: Vector3D, maximum: Vector3D) {
    this.set(minimum, maximum)
  }

  /**
   * Sets the given bounding box.
   *
   * @param bounds The bounds.
   * @return This bounding box for chaining.
   */
  fun set(bounds: BoundingBoxD): BoundingBoxD {
    return this.set(bounds.min, bounds.max)
  }

  /**
   * Sets the given minimum and maximum vector.
   *
   * @param minimum The minimum vector
   * @param maximum The maximum vector
   * @return This bounding box for chaining.
   */
  fun set(minimum: Vector3D, maximum: Vector3D): BoundingBoxD {
    min.set(
      if (minimum.x < maximum.x) minimum.x else maximum.x,
      if (minimum.y < maximum.y) minimum.y else maximum.y,
      if (minimum.z < maximum.z) minimum.z else maximum.z
    )
    max.set(
      if (minimum.x > maximum.x) minimum.x else maximum.x,
      if (minimum.y > maximum.y) minimum.y else maximum.y,
      if (minimum.z > maximum.z) minimum.z else maximum.z
    )
    this.update()
    return this
  }

  /**
   * Should be called if you modify [.min] and/or [.max] vectors manually.
   */
  fun update() {
    cnt.set(this.min).add(this.max).scl(0.5)
    dim.set(this.max).sub(this.min)
  }

  /**
   * Sets the bounding box minimum and maximum vector from the given points.
   *
   * @param points The points.
   * @return This bounding box for chaining.
   */
  fun set(points: Array<Vector3D>): BoundingBoxD {
    this.inf()
    for (lPoint in points) this.ext(lPoint)
    return this
  }

  /**
   * Sets the bounding box minimum and maximum vector from the given points.
   *
   * @param points The points.
   * @return This bounding box for chaining.
   */
  fun set(points: List<Vector3D>): BoundingBoxD {
    this.inf()
    for (lPoint in points) this.ext(lPoint)
    return this
  }

  /**
   * Sets the minimum and maximum vector to positive and negative infinity.
   *
   * @return This bounding box for chaining.
   */
  fun inf(): BoundingBoxD {
    min.set(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
    max.set(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
    cnt.set(0.0, 0.0, 0.0)
    dim.set(0.0, 0.0, 0.0)
    return this
  }

  /**
   * Extends the bounding box to incorporate the given [Vector3D].
   *
   * @param point The vector
   * @return This bounding box for chaining.
   */
  fun ext(point: Vector3D): BoundingBoxD {
    return this.set(
      min.set(
        min(
          min.x, point.x
        ), min(min.y, point.y), min(
          min.z, point.z
        )
      ),
      max.set(
        Math.max(max.x, point.x), Math.max(max.y, point.y), Math.max(
          max.z, point.z
        )
      )
    )
  }

  /**
   * Sets the minimum and maximum vector to zeros.
   *
   * @return This bounding box for chaining.
   */
  fun clr(): BoundingBoxD {
    return this.set(min.set(0.0, 0.0, 0.0), max.set(0.0, 0.0, 0.0))
  }

  val isValid: Boolean
    /**
     * Returns whether this bounding box is valid. This means that [.max] is greater than or equal to [.min].
     *
     * @return True in case the bounding box is valid, false otherwise
     */
    get() = min.x <= max.x && min.y <= max.y && min.z <= max.z

  /**
   * Extends this bounding box by the given bounding box.
   *
   * @param a_bounds The bounding box
   * @return This bounding box for chaining.
   */
  fun ext(a_bounds: BoundingBoxD): BoundingBoxD {
    return this.set(
      min.set(
        min(
          min.x, a_bounds.min.x
        ), min(min.y, a_bounds.min.y), min(
          min.z, a_bounds.min.z
        )
      ),
      max.set(
        max(max.x, a_bounds.max.x), max(
          max.y, a_bounds.max.y
        ), max(max.z, a_bounds.max.z)
      )
    )
  }

  /**
   * Extends this bounding box by the given sphere.
   *
   * @param center Sphere center
   * @param radius Sphere radius
   * @return This bounding box for chaining.
   */
  fun ext(center: Vector3D, radius: Double): BoundingBoxD {
    return this.set(
      min.set(
        min(
          min.x, center.x - radius
        ), min(min.y, center.y - radius), min(
          min.z, center.z - radius
        )
      ),
      max.set(
        max(max.x, center.x + radius), max(
          max.y, center.y + radius
        ), max(max.z, center.z + radius)
      )
    )
  }

  /**
   * Returns whether the given bounding box is contained in this bounding box.
   *
   * @param b The bounding box
   * @return Whether the given bounding box is contained
   */
  fun contains(b: BoundingBoxD): Boolean {
    return !this.isValid || (min.x <= b.min.x && min.y <= b.min.y && min.z <= b.min.z && max.x >= b.max.x && max.y >= b.max.y && max.z >= b.max.z)
  }

  /**
   * Returns whether the given bounding box is intersecting this bounding box (at least one point in).
   *
   * @param b The bounding box
   * @return Whether the given bounding box is intersected
   */
  fun intersects(b: BoundingBoxD): Boolean {
    if (!this.isValid) return false

    // test using SAT (separating axis theorem)
    val lx: Double = Math.abs(cnt.x - b.cnt.x)
    val sumx: Double = (dim.x / 2.0) + (b.dim.x / 2.0)

    val ly: Double = Math.abs(cnt.y - b.cnt.y)
    val sumy: Double = (dim.y / 2.0) + (b.dim.y / 2.0)

    val lz: Double = Math.abs(cnt.z - b.cnt.z)
    val sumz: Double = (dim.z / 2.0) + (b.dim.z / 2.0)

    return (lx <= sumx && ly <= sumy && lz <= sumz)
  }

  /**
   * Returns whether the given bounding box is intersecting this bounding box (at least one point in).
   *
   * @param b The bounding box
   * @return Whether the given bounding box is intersected
   */
  fun intersectsExclusive(b: BoundingBoxD): Boolean {
    if (!this.isValid) return false

    // test using SAT (separating axis theorem)
    val lx: Double = Math.abs(cnt.x - b.cnt.x)
    val sumx: Double = (dim.x / 2.0) + (b.dim.x / 2.0)

    val ly: Double = Math.abs(cnt.y - b.cnt.y)
    val sumy: Double = (dim.y / 2.0) + (b.dim.y / 2.0)

    val lz: Double = Math.abs(cnt.z - b.cnt.z)
    val sumz: Double = (dim.z / 2.0) + (b.dim.z / 2.0)

    return (lx < sumx && ly < sumy && lz < sumz)
  }

  /**
   * Returns whether the given vector is contained in this bounding box.
   *
   * @param v The vector
   * @return Whether the vector is contained or not.
   */
  fun contains(v: Vector3D): Boolean {
    return min.x <= v.x && max.x >= v.x && min.y <= v.y && max.y >= v.y && min.z <= v.z && max.z >= v.z
  }

  override fun toString(): String {
    return "[" + this.min + "|" + this.max + "]"
  }

  /**
   * Extends the bounding box by the given vector.
   *
   * @param x The x-coordinate
   * @param y The y-coordinate
   * @param z The z-coordinate
   * @return This bounding box for chaining.
   */
  fun ext(x: Double, y: Double, z: Double): BoundingBoxD {
    return this.set(
      min.set(
        min(
          min.x, x
        ), min(min.y, y), min(
          min.z, z
        )
      ),
      max.set(
        max(max.x, x), max(
          max.y, y
        ), max(max.z, z)
      )
    )
  }

  /**
   * Update the bounding box by the given deltas for x, y, and z coordinates.
   *
   * @param deltaX the change in the x coordinate
   * @param deltaY the change in the y coordinate
   * @param deltaZ the change in the z coordinate
   * @return the updated bounding box
   */
  fun updateByDelta(deltaX: Double, deltaY: Double, deltaZ: Double): BoundingBoxD {
    // Update the min and max coordinates based on the sign of the deltas
    if (deltaX < 0) min.x += deltaX
    else max.x += deltaX

    if (deltaY < 0) min.y += deltaY
    else max.y += deltaY

    if (deltaZ < 0) min.z += deltaZ
    else max.z += deltaZ

    this.update() // Update the bounding box
    return this // Return the updated bounding box
  }

  fun contains(x: Double, y: Double, z: Double): Boolean {
    return min.x <= x && max.x >= x && min.y <= y && max.y >= y && min.z <= z && max.z >= z
  }

  companion object {
    private val serialVersionUID = -1286036817192127343L

    fun min(a: Double, b: Double): Double {
      return if (a > b) b else a
    }

    fun max(a: Double, b: Double): Double {
      return if (a > b) a else b
    }
  }
}
