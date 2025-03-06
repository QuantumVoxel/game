package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi
import java.io.IOException
import kotlin.reflect.KClass

@ExperimentalApi
class PacketRegistry {
  val decoders = mutableMapOf<Int, PacketDecoder<*>>()
  val encoders = mutableMapOf<Int, PacketEncoder<*>>()
  val handlers = mutableMapOf<Int, PacketHandler<*>>()
  val registry = mutableMapOf<KClass<*>, Int>()

  fun <T : Any> register(id: Int, type: Class<T>, handler: PacketHandler<T>) {
    registry[type.kotlin] = id
    handlers[id] = handler
  }

  inline fun <reified T : Any> register(handler: PacketHandler<T>, id: Int = registry.size) {
    register(id, T::class.java, handler)
  }

  fun decode(buffer: PacketIO): Any? {
    val id = buffer.readInt()
    if (id == -1) return Disconnection(buffer.readString())
    val decoder = decoders[id] ?: return null
    return decoder.decode(buffer) as? Packet?
  }

  @Suppress("UNCHECKED_CAST")
  fun encode(buffer: PacketIO, packet: Any) {
    val id = registry[packet::class] ?: throw IOException("Unknown packet: ${packet::class.qualifiedName}")
    val encoder = encoders[id] ?: return
    (encoder as PacketEncoder<Any>).encode(buffer, buffer)
    buffer.writeInt(id)
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : Packet> handle(packet: T, context: PacketContext) {
    val handler = handlers[registry[packet::class]] as? PacketHandler<T> ?: return
    handler.handle(packet, context)
  }
}
