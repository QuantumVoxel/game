package dev.ultreon.quantum.dedicated

import dev.ultreon.quantum.PacketManager
import dev.ultreon.quantum.entity.Entity
import dev.ultreon.quantum.entity.EntityTemplate
import dev.ultreon.quantum.network.Networker
import dev.ultreon.quantum.network.Packet
import dev.ultreon.quantum.network.Player
import dev.ultreon.quantum.server.player.ServerPlayer
import io.socket.engineio.server.EngineIoServer
import io.socket.engineio.server.EngineIoServerOptions
import io.socket.engineio.server.transport.WebSocket
import io.socket.socketio.server.SocketIoNamespace
import io.socket.socketio.server.SocketIoServer
import io.socket.socketio.server.SocketIoServerOptions

class ServerSocketNetworker(val gameServer: DedicatedServer, val host: String, val port: Int) : Networker {
  private var closed: Boolean = false
  private lateinit var socketIoServer: SocketIoServer
  private val players: MutableMap<String, Player> = mutableMapOf()

  private lateinit var namespace: SocketIoNamespace

  override fun init() {
    socketIoServer = SocketIoServer(EngineIoServer(EngineIoServerOptions.newFromDefault().also {
      it.pingTimeout = 10000
    }), SocketIoServerOptions.newFromDefault().also {
      it.connectionTimeout = 10000
    })

    namespace = socketIoServer.namespace("/")
    namespace.apply {
      on("connection") { args ->
        val socket = args[0] as WebSocket
        if (closed) {
          socket.emit("disconnect", "Server closed")
          socket.close()
          return@on
        }
        println("Client " + socket.name + " (" + socket.initialHeaders["remote_addr"] + ") has connected.")
        socket.on("login") login@ {
          println("[Client " + socket.name + "] " + args)
          if (socket.name == null) {
            socket.emit("disconnect", "Disconnected")
            socket.close()
            return@login
          }

          if (args[0] == null) {
            socket.emit("disconnect", "Missing username")
            socket.close()
            return@login
          }

          val player = ServerPlayer(Entity(EntityTemplate.player, gameServer.dimension), args[0] as String, ServerTcpConnection(socket, this@apply))
          players[socket.name] = player

          socket.emit("login-ack")
          socket.emit("player-setup")

          socket.on("disconnect") {
            players.remove(socket.name)
            player.disconnect("Disconnected")
            socket.close()
          }

          socket.on("packet") {
            val packet = json.fromJson(Packet::class.java, it[0] as String)
            PacketManager.handlePacket(packet, player)
          }
        }
      }
    }
  }

  override fun close() {
    this.closed = true

    for (player in players.values) {
      player.disconnect("Server closed")
      player.connection.close()
    }

    namespace.off()
  }
}
