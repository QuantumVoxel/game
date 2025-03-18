package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi

@ExperimentalApi
abstract class Connection() {
  abstract fun sendPacket(packet: Packet, callback: () -> Unit = {})

  fun onDisconnect(reason: String) = Unit
  abstract fun disconnect(reason: String)
  abstract fun close()
}
