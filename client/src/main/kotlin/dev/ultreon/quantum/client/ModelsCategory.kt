package dev.ultreon.quantum.client

import dev.ultreon.quantum.resource.*

/**
 * The models category.
 * This category is used to store all models.
 *
 * @constructor Creates a new ModelsCategory.
 */
class ModelsCategory : ResourceCategory {
  override val name: String = "models"
  override val parent: ResourceCategory? = null

  val categories: MutableMap<String, ResourceCategory> = mutableMapOf()

  fun register(name: String, category: ResourceCategory) {
    categories[name] = category
  }

  override fun get(name: String): ResourceCategory? {
    return categories[name]
  }

  override fun get(domain: String, name: String): Resource? {
    return null
  }

  override fun iterator(): Iterator<Resource> {
    return iterator {  }
  }

  override fun set(domain: String, filename: String, value: StaticResource) {
    throw UnsupportedOperationException("ModelsCategory is category only")
  }
}
