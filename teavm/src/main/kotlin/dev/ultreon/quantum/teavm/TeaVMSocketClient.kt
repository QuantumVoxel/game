package dev.ultreon.quantum.teavm

import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import dev.ultreon.quantum.client.DisconnectedScreen
import dev.ultreon.quantum.client.network.ClientBaseSocket
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.network.*
import org.teavm.jso.typedarrays.ArrayBuffer
import org.teavm.jso.typedarrays.ArrayBufferView
import org.teavm.jso.typedarrays.Uint8Array
import org.teavm.jso.websocket.WebSocket
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class TeaVMSocketClient(address: String, stage: ConnectionStage) : ClientBaseSocket(address, stage) {
  private var socket: WebSocket = WebSocket("ws://$address/")

  private fun run() {
    socket.onMessage {
      val message = it as ArrayBufferView
      val uint8Array = Uint8Array(message)
      val byteArray = ByteArray(uint8Array.length)
      for (i in 0 until uint8Array.length) {
        byteArray[i] = uint8Array[i].toByte()
      }
      val input = DataInput(ByteArrayInputStream(byteArray))
      val io = PacketIO(input, DataOutput(OutputStream.nullOutputStream()))
      val decode = stage.collection.packetToClient.decode(io)

      if (decode is Packet) {
        decode.handle(TeaVMClientPacketContext(this, decode))
      }
    }

    socket.onClose {
      this.onDisconnected()
    }
  }

  private fun onDisconnected() {
    quantum.showScreen(DisconnectedScreen("Disconnected"))
  }

  override fun disconnect(reason: String) {
    this.socket.close()
  }

  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    val bos = ByteArrayOutputStream()
    val dataOutput = DataOutput(bos)
    packet.write(PacketIO(DataInput(InputStream.nullInputStream()), dataOutput))

    val arrayBuffer = ArrayBuffer(bos.size())
    Uint8Array(arrayBuffer).set(bos.toByteArray()).also {
      socket.send(arrayBuffer)
      bos.close()
      dataOutput.close()

      callback()
    }
  }

  override fun close() {
    socket.close()
  }
}

class TeaVMClientPacketContext(val client: TeaVMSocketClient, val packet: Packet) : PacketContext() {
  override fun reply(packet: Packet, callback: () -> Unit) {
    client.sendPacket(packet, callback)
  }

  override fun moveStage(stage: ConnectionStage) {
    client.stage = stage
  }

  override fun disconnect(reason: String) {
    client.disconnect(reason)
  }

  override val player: Player
    get() = quantum.player ?: error("Player is null")
}
