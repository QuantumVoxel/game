package dev.ultreon.quantum.client.resource

import dev.ultreon.quantum.client.texture.TextureManager
import dev.ultreon.quantum.resource.Resource
import dev.ultreon.quantum.resource.ResourceCategory
import dev.ultreon.quantum.resource.StaticResource

/**
 * The `textures` resource category.
 *
 * @param textureManager The texture manager.
 * @constructor Creates a new textures category.
 */
class TexturesCategory(val textureManager: TextureManager) : ResourceCategory {
  private val categories: MutableMap<String, TextureCategory> = mutableMapOf()
  override val parent: ResourceCategory? = null
  override val name = "textures"

  fun register(name: String, textureCategory: TextureCategory) {
    categories[name] = textureCategory
  }

  override fun get(name: String): ResourceCategory? {
    return categories[name]
  }

  override fun get(domain: String, name: String): Resource? {
    return null
  }

  override fun set(domain: String, filename: String, value: StaticResource) {
    throw UnsupportedOperationException("TexturesCategory is category only")
  }

  override fun iterator(): Iterator<Resource> {
    return iterator {  }
  }
}
