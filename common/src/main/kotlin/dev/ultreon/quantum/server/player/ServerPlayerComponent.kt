package dev.ultreon.quantum.server.player

import dev.ultreon.quantum.entity.ComponentType
import dev.ultreon.quantum.entity.PlayerComponent

/**
 * Represents a player on the server.
 * @param name The name of the player.
 * @constructor Creates a new server player component.
 *
 * @property name The name of the player.
 */
class ServerPlayerComponent(name: String) : PlayerComponent<ServerPlayerComponent>(name) {
  override val componentType: ComponentType<out ServerPlayerComponent>
    get() = ComponentType.Companion.serverPlayer
}
