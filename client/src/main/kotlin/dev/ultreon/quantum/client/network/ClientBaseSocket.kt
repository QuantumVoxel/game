package dev.ultreon.quantum.client.network

import dev.ultreon.quantum.network.BaseSocket
import dev.ultreon.quantum.network.ConnectionStage

/**
 * Represents a client-side socket.
 *
 * @param address The address of the socket.
 * @param stage The stage of the connection.
 */
abstract class ClientBaseSocket(val address: String, stage: ConnectionStage) : BaseSocket(stage)
