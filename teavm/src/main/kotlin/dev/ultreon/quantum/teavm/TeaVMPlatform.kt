package dev.ultreon.quantum.teavm

import com.badlogic.gdx.Gdx
import dev.ultreon.quantum.GamePlatform
import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.resource.ResourceManager
import org.teavm.jso.browser.Window

class TeaVMPlatform : GamePlatform {
  override val isWebGL3: Boolean
    get() = Gdx.gl30 != null

  override val isWebGL2: Boolean
    get() = Gdx.gl20 != null

  override fun loadResources(resourceManager: ResourceManager) {
    resourceManager.load(Gdx.files.internal("."))
  }

  override val isMobile: Boolean
    get() = TeaApplication.isMobileDevice()

  override val isWeb: Boolean
    get() = true

  override fun cpuCores(): Int {
    return 4
  }

  override fun yield() {
//    Thread.yield()
  }

  override fun createAsyncExecutor(maxConcurrent: Int, name: String): AsyncExecutor {
    return TeaVMAsyncExecutor(maxConcurrent, name)
  }

  override fun sleep(i: Int) {
//    Thread.sleep(i.toLong())
  }

  override fun <T> createFuture(): Future<T> {
    return TeaVMFuture()
  }

  override fun halt(i: Int) {
    Window.current().close()
  }

  override val isDebug: Boolean
    get() = Window.current().location.hostName == "localhost"
}
