package dev.ultreon.quantum.server

/**
 * Represents a storage of data.
 *
 * @param D The data type.
 */
interface Storage<D : Any> {
  /**
   * Sets the value at the given index.
   *
   * @param storageIndex The index.
   * @param value The value.
   * @return `true` if the value was set, `false` otherwise.
   */
  operator fun set(storageIndex: Int, value: D): Boolean

  /**
   * Gets the value at the given index.
   *
   * @param storageIndex The index.
   * @return The value.
   */
  operator fun get(storageIndex: Int): D

  /**
   * Maps the storage to a new storage.
   *
   * @param defaultValue The default value.
   * @param generator The generator.
   * @param mapper The mapper.
   * @return The new storage.
   */
  fun <R : Any> map(defaultValue: R, generator: (index: Int) -> Array<R>, mapper: (D) -> R): Storage<R>

  /**
   * Checks if the storage is uniform (aka. all values are the same).
   */
  val isUniform: Boolean
}
