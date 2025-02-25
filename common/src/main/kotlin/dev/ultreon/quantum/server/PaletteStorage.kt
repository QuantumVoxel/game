package dev.ultreon.quantum.server

import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.ExperimentalQuantumApi
import ktx.collections.GdxArray
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Function
import java.util.function.IntFunction

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
@ExperimentalQuantumApi
class PaletteStorage<D : Any> : Disposable, Storage<D> {
  private val defaultValue: D
  private var palette: ShortArray
  private var data = GdxArray<D>()
  private val rwLock = ReentrantReadWriteLock()

  @Deprecated("")
  constructor(defaultValue: D, size: Int) : this(size, defaultValue)

  constructor(defaultValue: D, palette: ShortArray, data: com.badlogic.gdx.utils.Array<D>) {
    this.defaultValue = defaultValue
    this.palette = palette
    this.data = data
  }

  constructor(size: Int, defaultValue: D) {
    this.defaultValue = defaultValue

    this.palette = ShortArray(size)
    Arrays.fill(this.palette, (-1).toShort())
  }

  override operator fun set(idx: Int, value: D): Boolean {
    val old = palette[idx]

    var setIdx = data.indexOf(value, false).toShort()
    if (setIdx.toInt() == -1) {
      setIdx = this.add(idx, value)
    }
    palette[idx] = setIdx

    if (old < 0 || old in this.palette) return false

    val i1 = data.indexOf(value, false)
    if (i1 >= 0) {
      data[old.toInt()] = value
      return false
    }

    data.removeIndex(old.toInt())

    // Update paletteMap entries for indices after the removed one
    for (i in palette.indices) {
      val oldValue = palette[i].toInt()
      palette[i] = (oldValue - 1).toShort()
    }
    return false
  }

  fun toDataIdx(idx: Int): Short {
    return if (idx >= 0 && idx < palette.size) palette[idx] else -1
  }

  fun direct(dataIdx: Int): D {
    if (dataIdx >= 0 && dataIdx < data.size) {
      val d = data[dataIdx]
      return d ?: this.defaultValue
    }

    return this.defaultValue
  }

  fun add(idx: Int, value: D): Short {
    val dataIdx = (data.size).toShort()
    data.add(value)
    palette[idx] = dataIdx
    return dataIdx
  }

  fun remove(idx: Int) {
    if (idx >= 0 && idx < data.size) {
      val dataIdx = toDataIdx(idx).toInt()
      if (dataIdx < 0) return
      data.removeIndex(dataIdx)
      palette[idx] = -1

      // Update paletteMap entries for indices after the removed one
      for (i in idx..<palette.size) {
        val oldValue = palette[i].toInt()
        palette[i] = (oldValue - 1).toShort()
      }
    }
  }

  override fun dispose() {
    data.clear()
  }

  override operator fun get(idx: Int): D {
    val paletteIdx = this.toDataIdx(idx)
    return if (paletteIdx < 0) this.defaultValue else this.direct(paletteIdx.toInt())
  }

  override fun <R : Any> map(defaultValue: R, generator: IntFunction<Array<R>>, mapper: Function<D, R>): Storage<R> {
    val ref = object : Any() {
      @Transient
      val mapperRef: Function<D, R> = mapper
    }

    val data = generator.apply(data.size)
    for (i in 0..<this.data.size) {
      val d = this.data[i]
      val applied: R = ref.mapperRef.apply(d)
      data[i] = applied
    }
    return PaletteStorage(defaultValue, this.palette, com.badlogic.gdx.utils.Array(data))
  }

  fun getPalette(): ShortArray {
    val palette = palette.clone()
    rwLock.readLock().unlock()
    return palette
  }

  fun getData(): List<D> {
    return data.toList()
  }

  fun set(palette: ShortArray, data: Array<D>) {
    set(palette, com.badlogic.gdx.utils.Array(data))
  }

  fun set(palette: ShortArray, data: com.badlogic.gdx.utils.Array<D>) {
    require(this.palette.size == palette.size) { "Palette length must be equal." }

    require(!this.data.contains(null, true)) { "Data cannot contain null values." }

    this.palette = palette
    this.data = data
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this.javaClass != other.javaClass) return false
    val that = other as PaletteStorage<*>
    return palette.contentEquals(that.palette) && this.data == that.data
  }

  override fun hashCode(): Int {
    var result = data.hashCode()
    result = 31 * result + palette.contentHashCode()
    return result
  }

  override val isUniform: Boolean
    get() = data.size <= 1
}
