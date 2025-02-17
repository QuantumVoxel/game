@file:JvmName("TeaVMLauncher")

package dev.ultreon.quantum.teavm

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.factory

/**
 * Launches the TeaVM/HTML application.
 */
fun main() {
  val config = TeaApplicationConfiguration("canvas").apply {
    width = 0
    height = 0
    antialiasing = false
    padVertical = 0
    padHorizontal = 0
    usePhysicalPixels = true
    powerPreference = "high-performance"

    useGL30 = true
  }

  factory = TeaVMLoggerFactory()

  gamePlatform = TeaVMPlatform()

  TeaApplication(QuantumVoxel(), config)
}
