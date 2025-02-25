package dev.ultreon.quantum.network.packets

import dev.ultreon.quantum.network.Packet
import dev.ultreon.quantum.network.PacketContext
import dev.ultreon.quantum.network.PacketIO

data class DisconnectPacket(val reason: String) : Packet("disconnect") {
  override fun write(io: PacketIO) {
    io.writeString(reason)
  }

  override fun handle(context: PacketContext) {
    context.disconnect(reason)
  }

  companion object : Factory<DisconnectPacket> {
    override fun read(io: PacketIO): DisconnectPacket {
      return DisconnectPacket(io.readString())
    }
  }
}
