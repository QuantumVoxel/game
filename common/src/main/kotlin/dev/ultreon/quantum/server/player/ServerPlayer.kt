package dev.ultreon.quantum.server.player

import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.entity.Entity
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.network.Player

/**
 * Represents a player on the server.
 *
 * @param entity The entity.
 * @param name The name of the player.
 * @param connection The connection of the player.
 *
 * @constructor Creates a new server player.
 */
@ExperimentalApi
class ServerPlayer(override val entity: Entity?,
                   override val name: String,
                   override val connection: Connection
) : Player() {

  /**
   * Called when a message is received.
   */
  override fun messageReceived(message: String) {
    TODO("Not yet implemented")
  }
}
