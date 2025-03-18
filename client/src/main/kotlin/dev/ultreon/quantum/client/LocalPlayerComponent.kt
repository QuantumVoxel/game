package dev.ultreon.quantum.client

import dev.ultreon.quantum.client.entity.ClientComponentTypes
import dev.ultreon.quantum.entity.ComponentType
import dev.ultreon.quantum.entity.PlayerComponent

/**
 * The local player component.
 * This component is used to store the local player's data.
 *
 * @param name The name of the component.
 * @constructor Creates a new LocalPlayerComponent.
 */
class LocalPlayerComponent @JvmOverloads constructor(name: String = "local") : PlayerComponent<LocalPlayerComponent>(name) {
  /**
   * The component type.
   */
  override val componentType: ComponentType<out LocalPlayerComponent> = ClientComponentTypes.localPlayer
}
