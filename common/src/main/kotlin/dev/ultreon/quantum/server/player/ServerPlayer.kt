package dev.ultreon.quantum.server.player

import dev.ultreon.quantum.entity.Entity
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.network.Player

class ServerPlayer(override val entity: Entity?,
                   override val name: String,
                   override val connection: Connection
) : Player() {
  override fun messageReceived(message: String) {
    TODO("Not yet implemented")
  }
}
