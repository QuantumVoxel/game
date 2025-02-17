package dev.ultreon.quantum.network

abstract class Connection() {
  abstract fun sendPacket(packet: Packet, callback: () -> Unit = {})

  fun onDisconnect(reason: String) = Unit
  abstract fun disconnect(reason: String)
  abstract fun close()
}
