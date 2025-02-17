package dev.ultreon.quantum.network

abstract class PacketContext {
  protected val connection: Connection? = null

  abstract fun reply(packet: Packet, callback: () -> Unit = {})
  abstract fun moveStage(stage: ConnectionStage)
  abstract fun disconnect(reason: String)

  abstract val player: Player
}
