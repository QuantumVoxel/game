package dev.ultreon.quantum.client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.SocketHints
import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import com.badlogic.gdx.utils.async.AsyncExecutor
import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.client.network.ClientBaseSocket
import dev.ultreon.quantum.network.ConnectionStage
import dev.ultreon.quantum.network.Packet
import dev.ultreon.quantum.network.PacketIO
import dev.ultreon.quantum.network.packets.DisconnectPacket
import kotlinx.coroutines.*
import ktx.async.AsyncExecutorDispatcher
import ktx.async.KtxAsync
import java.io.ByteArrayOutputStream
import java.util.*

const val BUFFER_SIZE = 2048

/**
 * This class is used to store partial packet data.
 *
 * @param id The ID of the packet.
 * @param bytes The bytes of the packet.
 * @param size The size of the packet.
 * @param currentSize The current size of the packet.
 * @constructor Creates a new partial packet data with the specified [id], [bytes], [size], and [currentSize].
 *
 * @see ClientSocket
 * @author <a href="https://github.con/XyperCode">Qubilux</a>
 */
data class PartialPacketData(val id: Short, val bytes: ByteArrayOutputStream, val size: Int, var currentSize: Int = 0)

/**
 * A client socket that connects to a server.
 * This socket is used to send and receive packets from the server.
 *
 * @param address The address of the server.
 * @param stage The stage of the connection.
 * @constructor Creates a new client socket with the specified [address] and [stage].
 * @see Net.newClientSocket
 */
@ExperimentalApi
class ClientSocket(address: String, stage: ConnectionStage) : ClientBaseSocket(address, stage) {
  private val socket = Gdx.net.newClientSocket(Net.Protocol.TCP, address, 38800, SocketHints().apply {
    keepAlive = true
    connectTimeout = 5000
    socketTimeout = 30000
  })

  private val availableProcessors = Runtime.getRuntime().availableProcessors()
  private val async = AsyncExecutorDispatcher(AsyncExecutor(availableProcessors), availableProcessors)

  private val dataIn = DataInput(socket.inputStream)
  private val dataOut = DataOutput(socket.outputStream)

  private val partialPackets = mutableMapOf<UUID, PartialPacketData>()

  private var readOnly = false

  /**
   * Disconnects the socket with the specified reason.
   *
   * @param reason The reason for disconnecting.
   */
  override fun disconnect(reason: String) {
    sendPacket(DisconnectPacket(reason)) {
      readOnly = true
    }
  }

  /**
   * Sends a packet through the socket and executes a callback upon completion.
   *
   * @param packet The packet to send.
   * @param callback The callback to execute after the packet is sent.
   */
  override fun sendPacket(packet: Packet, callback: () -> Unit) {
    ByteArrayOutputStream().use { bos ->
      DataOutput(bos).use { out ->
        PacketIO(null, out)
      }
      bos
    }.also {
      val bytes = it.toByteArray()
      KtxAsync.launch(async) {
        send(UUID.randomUUID(), bytes)
      }
    }
  }

  /**
   * Sends a packet through the socket.
   *
   * @param bytes The bytes to send.
   * @throws Exception If an error occurs while sending the packet.
   */
  @Throws(Exception::class)
  private suspend fun send(uuid: UUID, bytes: ByteArray) {
    withContext(Dispatchers.IO) {
      dataOut.writeShort(stage.collection.id)

      var sizeLeft = bytes.size
      for (off in 0..bytes.size step BUFFER_SIZE) {
        if (readOnly) return@withContext

        if (off != 0) {
          dataOut.writeShort(-1)
        } else {
          dataOut.writeInt(bytes.size, true)
        }
        dataOut.writeLong(uuid.mostSignificantBits)
        dataOut.writeLong(uuid.leastSignificantBits)

        val writeSize = sizeLeft.coerceAtMost(BUFFER_SIZE)
        dataOut.writeShort(writeSize)
        dataOut.write(bytes, off, writeSize)
        sizeLeft -= writeSize
        if (sizeLeft == 0) {
          dataOut.writeBoolean(false)
          break
        }
        dataOut.writeBoolean(true)
        yield()
      }
    }
  }

  /**
   * Reads the data from the socket until a [DisconnectPacket] is received.
   */
  fun reader() {
    var id: Short
    var size: Int = -1
    var readSize: Int
    var willContinue: Boolean
    while (true) {
      id = dataIn.readShort()

      if (id != (-1).toShort()) size = dataIn.readInt(true)

      val uuid = UUID(dataIn.readLong(), dataIn.readLong())
      readSize = dataIn.readShort().toInt()
      val bytes = dataIn.readNBytes(readSize)
      willContinue = dataIn.readBoolean()

      if (id == (-1).toShort()) read(id, uuid, bytes, willContinue)
      else read(id, uuid, bytes, willContinue, size)
    }
  }

  /**
   * Reads a packet from the socket.
   *
   * @param id The ID of the packet.
   * @param uuid The UUID of the packet.
   * @param bytes The bytes of the packet.
   * @param willContinue Whether the packet will continue.
   * @param size The size of the packet, if it isn't a partial packet.
   */
  fun read(id: Short, uuid: UUID, bytes: ByteArray, willContinue: Boolean, size: Int? = null) {
    if (id == (-1).toShort()) {
      readPartial(uuid, bytes, willContinue)
      return
    }

    if (willContinue) {
      partialPackets[uuid] = PartialPacketData(id, ByteArrayOutputStream().also {
        it.writeBytes(bytes)
      }, size!!)
    }
  }

  /**
   * Reads a partial packet from the socket.
   *
   * @param uuid The UUID of the partial packet.
   * @param bytes The bytes of the partial packet.
   * @param willContinue Whether the partial packet will continue.
   */
  private fun readPartial(uuid: UUID, bytes: ByteArray, willContinue: Boolean) {
    val partialPacketData = partialPackets[uuid] ?: return disconnect("Server Error: Non-existing partial packet")

    val newSize = partialPacketData.currentSize + bytes.size
    val expectedSize = partialPacketData.size

    if (willContinue && newSize > expectedSize)
      return disconnect("Server Error: Packet buffer overrun")
    if (!willContinue && newSize != expectedSize)
      return disconnect("Server Error: Packet buffer ${if (newSize < expectedSize) "underflow" else "overflow"}")

    partialPacketData.bytes.writeBytes(bytes)
    partialPacketData.currentSize += bytes.size
  }

  /**
   * Closes the socket.
   */
  override fun close() {
    socket.dispose()
  }
}
