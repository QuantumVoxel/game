package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.network.packets.ChatPacket

/**
 * Represents an audience that can receive packets.
 *
 * @see Connection
 * @see BaseSocket
 * @see Networker
 */
interface Audience {
  /**
   * Sends a packet to everyone in the audience.
   *
   * @param packet The packet to send.
   * @param callback The callback to be called when the packet is sent.
   */
  @ExperimentalApi
  fun sendPacket(packet: Packet, callback: () -> Unit = {})

  /**
   * Disconnects the entire audience.
   *
   * @param reason The reason for the disconnection.
   */
  fun disconnect(reason: String)

  /**
   * Sends a chat message to everyone in the audience.
   *
   * @param message The message to send.
   * @see ChatPacket
   */
  @ExperimentalApi
  fun sendMessage(message: String) {
    sendPacket(ChatPacket(message))
  }
}
