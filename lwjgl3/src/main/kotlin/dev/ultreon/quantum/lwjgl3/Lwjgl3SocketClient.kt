package dev.ultreon.quantum.lwjgl3

import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import dev.ultreon.quantum.client.DisconnectedScreen
import dev.ultreon.quantum.client.network.ClientBaseSocket
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.network.*
import dev.ultreon.quantum.network.packets.DisconnectPacket
import io.socket.engineio.client.Transport
import io.socket.engineio.client.transports.WebSocket
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class Lwjgl3SocketClient(address: String, stage: ConnectionStage, val initPacket: Packet, val stageAfterInit: ConnectionStage) : ClientBaseSocket(address, stage) {
  private var socket: WebSocket = WebSocket(Transport.Options().also {
    it.port = address.split(":")[1].toIntOrNull() ?: 38800
    it.hostname = address.split(":")[0]
    it.secure = false
    it.policyPort = 38800
  })

  override fun disconnect(reason: String) {
    sendPacket(DisconnectPacket(reason))
    socket.close()
  }

  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    val bos = ByteArrayOutputStream()
    val dataOutput = DataOutput(bos)

    packet.write(PacketIO(DataInput(InputStream.nullInputStream()), dataOutput))
    socket.emit("message", bos.toByteArray())
  }

  override fun close() {
    socket.close()
  }

  fun run() {
    socket.on("connect") {
      println("Connected")

      sendPacket(initPacket) {
        stage = stageAfterInit
      }
    }

    socket.on("disconnect") {
      println("Disconnected")

      onDisconnected()
    }

    socket.on("message") {
      val message = it[0] as ByteArray
      val input = DataInput(ByteArrayInputStream(message))
      val io = PacketIO(input, DataOutput(OutputStream.nullOutputStream()))
      val decode = stage.collection.packetToClient.decode(io)

      if (decode is Packet) {
        decode.handle(Lwjgl3ClientPacketContext(this, decode))
      }
    }
  }

  private fun onDisconnected() {
    quantum.showScreen(DisconnectedScreen("Disconnected"))
  }
}

class Lwjgl3ClientPacketContext(private val client: Lwjgl3SocketClient, private val packet: Packet) : PacketContext() {
  override fun reply(packet: Packet, callback: () -> Unit) {
    client.sendPacket(packet, callback)
  }

  override fun moveStage(stage: ConnectionStage) {
    client.stage = stage
  }

  override fun disconnect(reason: String) {
    client.disconnect(reason)
  }

  override val player: Player get() = quantum.player ?: error("Player is null")
}
