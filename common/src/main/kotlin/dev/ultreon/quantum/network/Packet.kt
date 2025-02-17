package dev.ultreon.quantum.network

abstract class Packet(val id: String) {
  abstract fun handle(context: PacketContext)
  open fun write(io: PacketIO) {}

  interface Factory<T : Packet> {
    fun read(io: PacketIO): T
  }
}
