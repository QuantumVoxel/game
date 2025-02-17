package dev.ultreon.quantum.client.network

import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.network.BaseSocket
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.network.Packet

class ClientConnection(quantumVoxel: QuantumVoxel, val socket: ClientBaseSocket) : Connection() {
  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    socket.sendPacket(packet, callback)
  }

  override fun disconnect(reason: String) {
    socket.disconnect(reason)
  }

  override fun close() {
    socket.close()
  }
}
