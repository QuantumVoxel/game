package dev.ultreon.quantum.dedicated

import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.network.Packet
import io.socket.engineio.server.transport.WebSocket
import io.socket.socketio.server.SocketIoNamespace

internal val json = Json(JsonWriter.OutputType.json)

class ServerTcpConnection(val socket: WebSocket, val namespace: SocketIoNamespace?) : Connection() {
  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    socket.emit("packet", json.toJson(packet))
  }

  override fun disconnect(reason: String) {
    logger.info("Disconnecting ${socket.name}: $reason")
    socket.close()
  }

  override fun close() {
    socket.close()
  }

  override fun toString(): String {
    return "ServerTcpConnection(${socket.name})"
  }
}
