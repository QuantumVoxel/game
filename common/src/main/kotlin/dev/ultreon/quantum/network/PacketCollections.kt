package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi

@ExperimentalApi
object PacketCollections {
  val handshake = PacketCollection("Handshake", 0) {
    registerServerEndpoint<ConnectionGuard> { connectionGuard, context -> connectionGuard.handle(context) }
    registerClientEndpoint<ConnectionAnswer> { connectionAnswer, context -> connectionAnswer.handle(context) }
    registerServerEndpoint<ConnectionResponse> { connectionResponse, context -> connectionResponse.handle(context) }
  }
  val status = PacketCollection("Status", 1) {

  }
  val login = PacketCollection("Login", 1) {

  }
  val play = PacketCollection("Play", 1) {

  }
}

@ExperimentalApi
data class ConnectionGuard(val question: Int) : Packet("ConnectionGuard") {

  override fun handle(context: PacketContext) {
    context.reply(ConnectionAnswer(question))
  }

  override fun write(io: PacketIO) {
    io.writeInt(question)
  }
}

@ExperimentalApi
data class ConnectionAnswer(val answer: Int) : Packet("ConnectionAnswer") {
  override fun handle(context: PacketContext) {
    context.reply(ConnectionResponse(answer % 64 == 0 && answer != 0)) {
      context.moveStage(ConnectionStage.LOGIN)
    }
  }

  override fun write(io: PacketIO) {
    io.writeInt(answer)
  }
}

@ExperimentalApi
data class ConnectionResponse(val accepted: Boolean) : Packet("ConnectionResponse") {

  override fun handle(context: PacketContext) {
    context.moveStage(ConnectionStage.LOGIN)
  }

  override fun write(io: PacketIO) {
    io.writeBoolean(accepted)
  }
}
