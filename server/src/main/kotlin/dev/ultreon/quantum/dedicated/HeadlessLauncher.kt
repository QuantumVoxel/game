@file:JvmName("HeadlessLauncher")

package dev.ultreon.quantum.dedicated

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import dev.ultreon.quantum.GamePlatform
import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.resource.ResourceManager

/**
 * Launches the dedicated server.
 *
 * @see DedicatedServer
 * @author Qubilux
 * @since 0.0.1
 */
fun main() {
  gamePlatform = object : GamePlatform {
    override val isServer: Boolean
      get() = true

    override fun loadResources(resourceManager: ResourceManager) {
      resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"))
    }

    override fun cpuCores(): Int {
      return Runtime.getRuntime().availableProcessors()
    }

    override fun yield() {
      Thread.yield()
    }

    override fun createAsyncExecutor(maxConcurrent: Int, name: String): AsyncExecutor {
      return HeadlessAsyncExecutor(maxConcurrent, name)
    }

    override fun sleep(i: Int) {
      Thread.sleep(i.toLong())
    }

    override fun <T> createFuture(): Future<T> {
      return HeadlessFuture<T>()
    }

    override fun halt(i: Int) {
      Runtime.getRuntime().halt(i)
    }
  }

  HeadlessApplication(DedicatedServer(), HeadlessApplicationConfiguration().apply {
    updatesPerSecond = 20
  })
}
