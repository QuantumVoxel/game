package dev.ultreon.quantum.world

import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.registry.Registry
import dev.ultreon.quantum.registry.RegistryKeys
import dev.ultreon.quantum.resource.ResourceId

abstract class Element<T : Any> {
  abstract val value: T
  abstract val registryId: ResourceId<Registry<T>>

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> cast(id: ResourceId<Registry<T>>): Element<T>? {
    if (id == registryId) {
      return (this as Element<T>)
    }

    return null
  }
}

class BlockElement(override val value: Block) : Element<Block>() {
  override val registryId: ResourceId<Registry<Block>> = RegistryKeys.blocks
}
