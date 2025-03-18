package dev.ultreon.quantum

import dev.ultreon.quantum.registry.Registries
import dev.ultreon.quantum.registry.Registry
import dev.ultreon.quantum.resource.ResourceId
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.util.asIdOrNull

/**
 * A base class for game content.
 *
 * @param T The type of the content.
 * @property registry The registry of the content.
 * @constructor Creates a new game content with the specified registry.
 * @see Registry
 */
open class GameContent<T : Any>(private val registry: ResourceId<Registry<T>>) {
  @Suppress("UNCHECKED_CAST")
  val id: Registry<T> get() = Registries.get(registry, javaClass as Class<T>)

  /**
   * Registers the object with the given [name].
   */
  fun register(name: String) {
    @Suppress("UNCHECKED_CAST")
    id.register(name.asIdOrNull() ?: throw IllegalArgumentException("Invalid ID: $name"), this as T)
  }

  /**
   * Registers the object with the given [NamespaceID].
   */
  fun register(name: NamespaceID) {
    @Suppress("UNCHECKED_CAST")
    id.register(name, this as T)
  }
}
