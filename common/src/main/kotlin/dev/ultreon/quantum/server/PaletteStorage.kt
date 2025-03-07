package dev.ultreon.quantum.server

import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.ExperimentalApi
import ktx.collections.GdxArray
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 *
 * Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.
 *
 *
 * It makes use of short arrays to store [index pointers][.getPalette] to the [data][.getData].
 * While the data itself is stored without any duplicates.
 *
 * @param <D> the data type.
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
</D> */
@ExperimentalApi
class PaletteStorage<D : Any> : Disposable, Storage<D> {
  private val defaultValue: D
  private var referenceTable: ShortArray
  private var internal = GdxArray<D>()
  private val rwLock = ReentrantReadWriteLock()

  /**
   * Creates a new palette storage with the given default value and size.
   * The default value is used when the index is out of bounds.
   *
   * @param defaultValue The default value.
   * @param size The size of the storage.
   * @see set
   */
  @Deprecated("A better constructor is available.", ReplaceWith("PaletteStorage(size, defaultValue)"))
  constructor(defaultValue: D, size: Int) : this(size, defaultValue)

  /**
   * Creates a new palette storage with the given default value, palette, and data.
   * The default value is used when the index is out of bounds.
   *
   * @param defaultValue The default value.
   * @param palette The palette.
   * @param data The data.
   * @see set
   */
  constructor(defaultValue: D, palette: ShortArray, data: com.badlogic.gdx.utils.Array<D>) {
    this.defaultValue = defaultValue
    this.referenceTable = palette
    this.internal = data
  }

  /**
   * Creates a new palette storage with the given size and default value.
   * The default value is used when the index is out of bounds.
   *
   * @param size The size of the storage.
   * @param defaultValue The default value.
   * @see set
   */
  constructor(size: Int, defaultValue: D) {
    this.defaultValue = defaultValue

    this.referenceTable = ShortArray(size)
    Arrays.fill(this.referenceTable, (-1).toShort())
  }

  /**
   * Sets the value at the given index.
   * If the value already exists, it returns `false`.
   * Otherwise, it adds the value to the data and returns `true`.
   *
   * @param storageIndex The storage index.
   * @param value The value to set.
   * @return `true` if the value was set, `false` otherwise.
   * @see add
   */
  override operator fun set(storageIndex: Int, value: D): Boolean {
    val old = referenceTable[storageIndex]

    var setIdx = internal.indexOf(value, false).toShort()
    if (setIdx.toInt() == -1) {
      setIdx = this.add(storageIndex, value)
    }
    referenceTable[storageIndex] = setIdx

    if (old < 0 || old in this.referenceTable) return false

    val i1 = internal.indexOf(value, false)
    if (i1 >= 0) {
      internal[old.toInt()] = value
      return false
    }

    internal.removeIndex(old.toInt())

    // Update paletteMap entries for indices after the removed one
    for (i in referenceTable.indices) {
      val oldValue = referenceTable[i].toInt()
      referenceTable[i] = (oldValue - 1).toShort()
    }
    return false
  }

  /**
   * Returns the reference index of the value at the given index.
   * If the index is out of bounds, it returns -1.
   *
   * @param storageIndex The index.
   * @return The reference index of the value.
   */
  fun toInternalIndex(storageIndex: Int): Short {
    return if (storageIndex >= 0 && storageIndex < referenceTable.size) referenceTable[storageIndex] else -1
  }

  /**
   * Returns the value at the given index.
   * If the index is out of bounds, it returns the default value.
   *
   * @param internalIndex The internal data index.
   * @return The value.
   */
  fun direct(internalIndex: Int): D {
    if (internalIndex >= 0 && internalIndex < internal.size) {
      val d = internal[internalIndex]
      return d ?: this.defaultValue
    }

    return this.defaultValue
  }

  /**
   * Adds a value to the palette.
   * If the value already exists, it returns the index of the existing value.
   * Otherwise, it adds the value to the data and returns the index.
   *
   * @param idx The reference index.
   * @param value The value.
   * @return The storage index of the value.
   */
  fun add(idx: Int, value: D): Short {
    val dataIdx = (internal.size).toShort()
    internal.add(value)
    referenceTable[idx] = dataIdx
    return dataIdx
  }

  /**
   * Removes the value at the given index.
   * If the index is out of bounds, it does nothing.
   *
   * @param idx The index.
   */
  fun remove(idx: Int) {
    if (idx < 0 || idx >= internal.size) return
    val dataIdx = toInternalIndex(idx).toInt()
    if (dataIdx < 0) return
    internal.removeIndex(dataIdx)
    referenceTable[idx] = -1

    // Update paletteMap entries for indices after the removed one
    for (i in idx..<referenceTable.size) {
      val oldValue = referenceTable[i].toInt()
      referenceTable[i] = (oldValue - 1).toShort()
    }
  }

  /**
   * Clears the storage.
   */
  override fun dispose() {
    internal.clear()
  }

  /**
   * Gets the value at the given storage index.
   */
  override operator fun get(storageIndex: Int): D {
    val paletteIdx = this.toInternalIndex(storageIndex)
    return if (paletteIdx < 0) this.defaultValue else this.direct(paletteIdx.toInt())
  }

  /**
   * Maps the storage to a new storage.
   *
   * @param defaultValue The default value.
   * @param generator The generator.
   * @param mapper The mapper.
   * @return The new storage.
   */
  override fun <R : Any> map(defaultValue: R, generator: (Int) -> Array<R>, mapper: (D) -> R): Storage<R> {
    val ref = object : Any() {
      @Transient
      val mapperRef = mapper
    }

    val internalData = generator(internal.size)
    for (i in 0..<this.internal.size) {
      val internalDataElem = this.internal[i]
      val applied: R = ref.mapperRef(internalDataElem)
      internalData[i] = applied
    }
    return PaletteStorage(defaultValue, this.referenceTable, GdxArray(internalData))
  }

  /**
   * A copy of the reference table.
   */
  val references: ShortArray
    get() = referenceTable.clone()

  /**
   * A copy of the internal data.
   */
  val internalData: List<D>
    get() = internal.toList()

  /**
   * Sets the reference table and internal data.
   *
   * @param referenceTable The reference table.
   * @param internalData The internal data (as a JVM array).
   */
  fun set(referenceTable: ShortArray, internalData: Array<D>) {
    set(referenceTable, GdxArray(internalData))
  }

  /**
   * Sets the reference table and internal data.
   *
   * @param referenceTable The reference table.
   * @param internalData The internal data (as a libGDX array).
   */
  fun set(referenceTable: ShortArray, internalData: GdxArray<D>) {
    require(this.referenceTable.size == referenceTable.size) { "Reference table length must be equal." }
    require(!this.internal.contains(null, true)) { "Internal data cannot contain null values." }

    this.referenceTable = referenceTable
    this.internal = internalData
  }

  /**
   * Checks if this palette is equal to the other one.
   *
   * @param other The other palette.
   * @return `true` if the palettes are equal, `false` otherwise.
   */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this.javaClass != other.javaClass) return false
    val that = other as PaletteStorage<*>
    return referenceTable.contentEquals(that.referenceTable) && this.internal == that.internal
  }

  /**
   * Returns the hash code of this palette.
   *
   * @return The hash code.
   */
  override fun hashCode(): Int {
    var result = internal.hashCode()
    result = 31 * result + referenceTable.contentHashCode()
    return result
  }

  /**
   * Checks if this palette is uniform (aka. contains only one value).
   */
  override val isUniform: Boolean
    get() = internal.size <= 1
}
