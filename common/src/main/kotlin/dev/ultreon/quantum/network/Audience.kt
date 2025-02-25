package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalQuantumApi
import dev.ultreon.quantum.network.packets.ChatPacket

interface Audience {
  fun sendPacket(packet: Packet, callback: () -> Unit = {})

  fun disconnect(reason: String)
  @ExperimentalQuantumApi
  fun sendMessage(message: String) {
    sendPacket(ChatPacket(message))
  }
}
