package dev.ultreon.quantum.dedicated

import com.badlogic.gdx.ApplicationListener
import dev.ultreon.quantum.network.Networker
import dev.ultreon.quantum.server.QuantumVoxelServer

class DedicatedServer : QuantumVoxelServer(), ApplicationListener {
  override lateinit var networker: Networker
  override val isDedicatedServer: Boolean = true

  override fun create() {
    networker = ServerSocketNetworker(this, "localhost", 38800)
    networker.init()
  }

  override fun resize(width: Int, height: Int) {
    // No-op
  }

  override fun render() {
    this.runTick()
  }

  override fun pause() {
    // No-op
  }

  override fun resume() {
    // No-op
  }

  override fun dispose() {
    networker.close()
  }
}
