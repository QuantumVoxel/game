package dev.ultreon.quantum.resource

/**
 * Represents a resource category.
 *
 * @property name The name of the resource category.
 * @property parent The parent resource category.
 */
interface ResourceCategory : Iterable<Resource> {
  val name: String
  val parent: ResourceCategory?

  /**
   * Gets a resource category by name.
   *
   * @param name The name of the resource category.
   * @return The resource category.
   */
  operator fun get(name: String): ResourceCategory?

  /**
   * Gets a resource by domain and name.
   *
   * @param domain The domain of the resource.
   * @param name The name of the resource.
   * @return The resource.
   */
  operator fun get(domain: String, name: String): Resource?

  /**
   * Sets a resource by domain and filename.
   *
   * @param domain The domain of the resource.
   * @param filename The filename of the resource.
   * @param value The value of the resource.
   */
  operator fun set(domain: String, filename: String, value: StaticResource) {
    this[domain, filename] = value
  }

  /**
   * The iterator for the resource category.
   *
   * @return The iterator.
   */
  override operator fun iterator(): Iterator<Resource>
}
