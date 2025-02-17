package dev.ultreon.quantum.server.integrated

import dev.ultreon.quantum.server.QuantumVoxelServer

class IntegratedServer : QuantumVoxelServer() {
  override val isDedicatedServer = false
  override fun create() {
    TODO("Not yet implemented")
  }

  override fun resize(p0: Int, p1: Int) {
    TODO("Not yet implemented")
  }

  override fun render() {
    TODO("Not yet implemented")
  }

  override fun pause() {
    TODO("Not yet implemented")
  }

  override fun resume() {
    TODO("Not yet implemented")
  }

  override fun dispose() {
    TODO("Not yet implemented")
  }

  override val networker: MemoryNetworker = MemoryNetworker()
}
