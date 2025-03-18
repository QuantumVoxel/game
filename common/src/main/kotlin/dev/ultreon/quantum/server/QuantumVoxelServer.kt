package dev.ultreon.quantum.server

import com.badlogic.gdx.ApplicationListener
import dev.ultreon.quantum.network.Networker
import dev.ultreon.quantum.server.integrated.IntegratedServer
import dev.ultreon.quantum.world.Dimension

/**
 * The amount of ticks per second.
 * This is always 20.
 */
const val TPS = 20

/**
 * The amount of milliseconds per tick.
 * This is always 50.
 * (1000 / [TPS])
 */
const val MSPT = 1000 / TPS

/**
 * A server for Quantum Voxel. (Either [IntegratedServer] or DedicatedServer (see [isDedicatedServer])).
 *
 * @author Qubilux
 * @since 0.0.0
 */
abstract class QuantumVoxelServer : ApplicationListener {
  /**
   * The dimension that the server is running on.
   */
  val dimension: Dimension = ServerDimension()

  /**
   * The networker that the server is using.
   */
  abstract val networker: Networker

  /**
   * This does one update tick of the server.
   */
  fun runTick() {

  }

  /**
   * The server main loop.
   */
  private fun loop() {
    var lastTick = System.currentTimeMillis()
    while (true) {
      val currentTick = System.currentTimeMillis()
      val delta = currentTick - lastTick
      if (delta >= MSPT) {
        lastTick = currentTick
        runTick()
      }
    }
  }

  /**
   * Whether the server is a dedicated one (true) or a integrated one (false).
   */
  abstract val isDedicatedServer: Boolean
}

//val serverEventBus = EventBus()
