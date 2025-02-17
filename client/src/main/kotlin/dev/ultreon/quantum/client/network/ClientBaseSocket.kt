package dev.ultreon.quantum.client.network

import dev.ultreon.quantum.network.BaseSocket
import dev.ultreon.quantum.network.ConnectionStage

abstract class ClientBaseSocket(val address: String, stage: ConnectionStage) : BaseSocket(stage)
