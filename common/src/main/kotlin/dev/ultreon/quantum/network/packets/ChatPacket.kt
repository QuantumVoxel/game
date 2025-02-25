package dev.ultreon.quantum.network.packets

import dev.ultreon.quantum.network.Packet
import dev.ultreon.quantum.network.PacketContext
import dev.ultreon.quantum.network.PacketIO

data class ChatPacket(val message: String) : Packet("chat") {
  override fun handle(context: PacketContext) {
    context.player.messageReceived(message)
  }

  override fun write(io: PacketIO) {
    io.writeString(message)
  }

  companion object : Factory<ChatPacket> {
    override fun read(io: PacketIO): ChatPacket {
      return ChatPacket(io.readString())
    }
  }
}
