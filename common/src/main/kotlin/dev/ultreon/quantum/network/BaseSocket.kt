package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi

/**
 * Represents a socket that can send and receive packets.
 *
 * @param stage The stage of the connection.
 * @constructor Creates a new socket with the specified stage.
 */
@ExperimentalApi
abstract class BaseSocket(var stage: ConnectionStage) : Audience {
  /**
   * Disconnects the socket with the specified reason.
   *
   * @param reason The reason for disconnecting.
   */
  abstract override fun disconnect(reason: String)

  /**
   * Sends a packet through the socket and executes a callback upon completion.
   *
   * @param packet The packet to send.
   * @param callback The callback to execute after the packet is sent.
   */
  abstract override fun sendPacket(packet: Packet, callback: () -> Unit)

  /**
   * Closes the socket.
   */
  abstract fun close()
}
