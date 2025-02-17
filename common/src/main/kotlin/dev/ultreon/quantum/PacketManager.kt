package dev.ultreon.quantum

import dev.ultreon.quantum.network.Packet
import dev.ultreon.quantum.network.PacketContext
import dev.ultreon.quantum.server.player.ServerPlayer

object PacketManager {
  val packets = HashMap<String, Class<out Packet>>()
  val packetNames = HashMap<Class<out Packet>, String>()

  val packetHandlers = HashMap<String, (Packet, ServerPlayer?) -> Unit>()

  fun registerPacket(packet: Class<out Packet>, handler: (Packet, ServerPlayer?) -> Unit) {
    packets[packet.name] = packet
    packetNames[packet] = packet.simpleName
    packetHandlers[packet.name] = handler
  }

  fun unregisterPacket(packet: Class<out Packet>) {
    packets.remove(packet.name)
    packetNames.remove(packet)
    packetHandlers.remove(packet.name)
  }

  fun handlePacket(packet: Packet, context: ServerPlayer?) {
    packetHandlers[packet::class.java.name]?.invoke(packet, context)
  }

  fun unregisterAll() {
    packets.clear()
    packetNames.clear()
    packetHandlers.clear()
  }

  fun getRegisteredPackets() = packets.toMap()
}
