package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.InternalApi

@ExperimentalApi
class PacketCollection(val name: String, val id: Int, val builder: PacketCollector.() -> Unit) {
  val packetToClient = PacketRegistry()
  val packetToServer = PacketRegistry()

  init {
    builder(PacketCollector(this))
  }
}

@OptIn(InternalApi::class)
@ExperimentalApi
class PacketCollector(@property:InternalApi val collection: PacketCollection) {
  fun <T : Any> registerClientEndpoint(
    id: Int,
    type: Class<T>,
    handler: PacketHandler<T>
  ) {
    collection.packetToClient.register(id, type, handler)
  }

  fun <T : Any> registerServerEndpoint(
    id: Int,
    type: Class<T>,
    handler: PacketHandler<T>
  ) {
    collection.packetToServer.register(id, type, handler)
  }

  inline fun <reified T : Any> registerClientEndpoint(
    handler: PacketHandler<T>
  ) {
    collection.packetToClient.register(handler)
  }

  inline fun <reified T : Any> registerServerEndpoint(
    handler: PacketHandler<T>
  ) {
    collection.packetToServer.register(handler)
  }
}

fun interface PacketEncoder<T : Any> {
  fun encode(packet: T, buffer: PacketIO)
}

fun interface PacketDecoder<T : Any> {
  fun decode(buffer: PacketIO): T
}

fun interface PacketHandler<T : Any> {
  fun handle(packet: T, context: PacketContext)
}
