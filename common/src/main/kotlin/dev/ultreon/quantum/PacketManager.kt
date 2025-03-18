package dev.ultreon.quantum

import dev.ultreon.quantum.network.Packet
import dev.ultreon.quantum.server.player.ServerPlayer
import kotlin.reflect.KClass

/**
 * Manages packets and their handlers.
 *
 * ### Example usage:
 * ```kotlin
 * class MyPacket : Packet() {
 *   override fun handle(context: PacketContext) {
 *     // Handle the packet
 *   }
 *
 *   override fun write(io: PacketIO) {
 *     // Write the packet
 *   }
 *
 *   override fun read(io: PacketIO) {
 *     // Read the packet
 *   }
 * }
 *
 * PacketManager.registerPacket(MyPacket::class.java) { packet, player ->
 *   // Handle the packet
 * }
 * ```
 */
@ExperimentalApi
object PacketManager {
  val packets = HashMap<String, KClass<out Packet>>()
  val packetNames = HashMap<KClass<out Packet>, String>()

  val packetHandlers = HashMap<String, (Packet, ServerPlayer?) -> Unit>()

  /**
   * Registers a packet with the specified handler.
   *
   * @param packet The packet to register.
   * @param handler The handler for the packet.
   */
  fun <T : Packet> registerPacket(packet: KClass<T>, handler: (T, ServerPlayer?) -> Unit) {
    val name = packet.simpleName
      ?: packet.qualifiedName
      ?: packet.java.simpleName
    packets[name] = packet
    packetNames[packet] = name
    packetHandlers[name] = handler as (Packet, ServerPlayer?) -> Unit
  }

  fun unregisterPacket(packet: KClass<out Packet>) {
    val name = packetNames[packet] ?: return

    packets.remove(name)
    packetNames.remove(packet)
    packetHandlers.remove(name)
  }

  inline fun <reified T : Packet> registerPacket(noinline handler: (T, ServerPlayer?) -> Unit) {
    registerPacket(T::class, handler)
  }

  inline fun <reified T : Packet> unregisterPacket() {
    unregisterPacket(T::class)
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
