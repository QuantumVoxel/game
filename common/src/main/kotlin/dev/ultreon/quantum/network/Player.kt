package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.entity.Entity

/**
 * Represents a player on the server.
 *
 * @property entity The entity.
 * @property name The name of the player.
 * @property connection The connection of the player.
 *
 * @constructor Creates a new player.
 */
abstract class Player : Audience {
  abstract val entity: Entity?
  abstract val name: String
  abstract val connection: Connection

  override fun equals(other: Any?): Boolean {
    if (other is Player) {
      return entity == other.entity
    }
    return false
  }

  override fun hashCode(): Int {
    return entity?.hashCode() ?: 0
  }

  override fun toString(): String {
    return "Player('$name')"
  }

  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    connection.sendPacket(packet, callback)
  }

  @ExperimentalApi
  override fun sendMessage(message: String) {
    // TODO
  }

  override fun disconnect(reason: String) {
    connection.onDisconnect(reason)
  }

  abstract fun messageReceived(message: String)
}
