package dev.ultreon.quantum.client.resource

import dev.ultreon.quantum.client.texture.TextureManager
import dev.ultreon.quantum.resource.Resource
import dev.ultreon.quantum.resource.ResourceCategory
import dev.ultreon.quantum.resource.StaticResource
import dev.ultreon.quantum.util.NamespaceID

/**
 * A texture resource category.
 *
 * @param parent The parent category.
 * @param textureManager The texture manager.
 * @param name The name of the category.
 * @constructor Creates a new texture category with the specified parameters.
 */
open class TextureCategory(override val parent: TexturesCategory, val textureManager: TextureManager, override val name: String) : ResourceCategory {
  private val resources = HashMap<String, Resource>()

  override fun get(domain: String, name: String): Resource? {
    return resources["$domain:$name"]
  }

  override fun set(domain: String, filename: String, value: StaticResource) {
    resources["$domain:$filename"] = value
  }

  override fun iterator(): Iterator<Resource> {
    return resources.values.iterator()
  }

  operator fun set(id: NamespaceID, value: Resource) {
    resources[id.toString()] = value
  }

  override fun get(name: String): ResourceCategory? {
    return null
  }
}
