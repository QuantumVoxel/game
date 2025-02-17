package dev.ultreon.quantum.network

abstract class BaseSocket(var stage: ConnectionStage) : Audience {
  abstract override fun disconnect(reason: String)
  abstract override fun sendPacket(packet: Packet, callback: () -> Unit)

  abstract fun close()
}
