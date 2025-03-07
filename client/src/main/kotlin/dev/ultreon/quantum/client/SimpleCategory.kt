package dev.ultreon.quantum.client

import dev.ultreon.quantum.logger
import dev.ultreon.quantum.resource.Resource
import dev.ultreon.quantum.resource.ResourceCategory
import dev.ultreon.quantum.resource.StaticResource

/**
 * A simple resource category.
 *
 * @property name The name of the category.
 * @property parent The parent category.
 * @constructor Creates a new SimpleCategory.
 */
class SimpleCategory(override val name: String, override val parent: ResourceCategory?) : ResourceCategory {
  val resources: MutableMap<String, Resource> = mutableMapOf()

  /**
   * Gets a resource category by name.
   *
   * @param name The name of the resource category.
   * @return The resource category.
   */
  override fun get(name: String): ResourceCategory? {
    return null
  }

  /**
   * Gets a resource by domain and name.
   *
   * @param domain The domain of the resource.
   * @param name The name of the resource.
   * @return The resource.
   */
  override fun get(domain: String, name: String): Resource? {
    return resources["$domain:$name"]
  }

  /**
   * Sets a resource by domain and filename.
   *
   * @param domain The domain of the resource.
   * @param filename The filename of the resource.
   * @param value The value of the resource.
   */
  override fun set(domain: String, filename: String, value: StaticResource) {
    logger.debug("Setting resource: $domain:$filename")
    resources["$domain:$filename"] = value
  }

  /**
   * The iterator for the resource category.
   *
   * @return The iterator.
   */
  override fun iterator(): Iterator<Resource> {
    return resources.values.iterator()
  }
}
