package dev.ultreon.quantum.client.network

import dev.ultreon.quantum.DiscouragedApi
import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.network.BaseSocket
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.network.Packet

/**
 * Represents a client-side connection.
 *
 * @param quantumVoxel The client instance.
 * @param socket The socket of the connection.
 * @constructor Creates a new client-side connection with the specified client instance and [socket].
 */
@ExperimentalApi
class ClientConnection(quantumVoxel: QuantumVoxel, val socket: ClientBaseSocket) : Connection() {
  /**
   * Sends a packet to the server.
   *
   * @param packet The packet to send.
   * @param callback The callback to be called when the packet is sent.
   */
  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    socket.sendPacket(packet, callback)
  }

  /**
   * Disconnects the client from the server.
   *
   * @param reason The reason for the disconnection.
   */
  override fun disconnect(reason: String) {
    socket.disconnect(reason)
  }

  /**
   * Closes the connection without any reason.
   *
   * **It's not recommended to use this method.**
   */
  @DiscouragedApi
  override fun close() {
    socket.close()
  }
}
