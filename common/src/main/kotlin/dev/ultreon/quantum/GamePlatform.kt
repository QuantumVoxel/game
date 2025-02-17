package dev.ultreon.quantum

import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.network.BaseSocket
import dev.ultreon.quantum.resource.ResourceManager
import kotlin.system.exitProcess

/**
 * Represents the platform that the game is running on.
 *
 * @since 0.0.0
 */
interface GamePlatform {
  val isDesktop: Boolean get() = false
  val isMac: Boolean get() = false

  val isWindows: Boolean get() = false

  val isLinux: Boolean get() = false

  val isAndroid: Boolean get() = false

  val isIos: Boolean get() = false

  /**
   * Returns `true` if the game is running on the client, `false` if the game is running on the server.
   *
   * @since 0.0.2
   */
  val isClient: Boolean get() = !isServer

  /**
   * Returns `true` if the game is running on the server, `false` if the game is running on the client.
   *
   * @since 0.0.2
   */
  val isServer: Boolean get() = false

  /**
   * Returns `true` if the game is running on OpenGL ES 3.0.
   */
  val isGLES3: Boolean get() = false

  /**
   * Returns `true` if the game is running on OpenGL ES 2.0.
   */
  val isGLES2: Boolean get() = isGLES3

  /**
   * Returns `true` if the game is running on OpenGL 4.6.
   */
  val isGL46: Boolean get() = false

  /**
   * Returns `true` if the game is running on OpenGL 4.5.
   */
  val isGL45: Boolean get() = isGL46

  /**
   * Returns `true` if the game is running on OpenGL 4.4.
   */
  val isGL44: Boolean get() = isGL45

  /**
   * Returns `true` if the game is running on OpenGL 4.3.
   */
  val isGL43: Boolean get() = isGL44

  /**
   * Returns `true` if the game is running on OpenGL 4.2.
   */
  val isGL42: Boolean get() = isGL43

  /**
   * Returns `true` if the game is running on OpenGL 4.1.
   */
  val isGL41: Boolean get() = isGL42

  /**
   * Returns `true` if the game is running on OpenGL 4.0.
   */
  val isGL40: Boolean get() = isGL41

  /**
   * Returns `true` if the game is running on OpenGL 3.3.
   */
  val isGL33: Boolean get() = isGL40

  /**
   * Returns `true` if the game is running on OpenGL 3.2.
   */
  val isGL32: Boolean get() = isGL33

  /**
   * Returns `true` if the game is running on OpenGL 3.1.
   */
  val isGL31: Boolean get() = isGL32

  /**
   * Returns `true` if the game is running on OpenGL 3.0.
   */
  val isGL30: Boolean get() = isGL31

  /**
   * Returns `true` if the game is running on OpenGL 2.0.
   */
  val isGL20: Boolean get() = isGL30

  /**
   * Loads the game's resources.
   */
  fun loadResources(resourceManager: ResourceManager)

  /**
   * Disposes of the game's resources.
   */
  fun dispose() {

  }

  /**
   * Returns `true` if the game is running on a mobile platform, `false` if the game is running on a desktop platform.
   *
   * @since 0.0.2
   */
  val isMobile: Boolean get() = false

  /**
   * Handles next-frame logic. Useful for when a platform doesn't do something every frame that it should.
   */
  fun nextFrame() {

  }

  fun createClientSocket(asString: String): BaseSocket? {
    return null
  }

  /**
   * Returns `true` if the game is running on WebGL 3.0.
   */
  val isWebGL3: Boolean get() = false

  /**
   * Returns `true` if the game is running on WebGL 2.0.
   */
  val isWebGL2: Boolean get() = false

  /**
   * Returns `true` if the game is running using SwitchGDX backend.
   */
  val isSwitchGDX: Boolean get() = false

  /**
   * Returns `true` if the game is running on Switch.
   */
  val isSwitch: Boolean get() = isSwitchGDX

  /**
   * Returns `true` if the game is running on UWP.
   */
  val isUWP: Boolean get() = false

  /**
   * Returns `true` if the game is running in debug mode, `false` if the game is running in release mode.
   *
   * @since 0.0.2
   */
  val isDebug: Boolean get() = false

  /**
   * Returns `true` if the game is running on a web platform, `false` if the game is running on a desktop platform.
   *
   * @since 0.0.2
   */
  val isWeb: Boolean get() = isWebGL3 || isWebGL2

  fun halt(i: Int)
  fun cpuCores(): Int
  fun yield()

  fun createAsyncExecutor(maxConcurrent: Int, name: String = "AsyncExecutor-Thread"): AsyncExecutor
  fun sleep(i: Int)
  fun <T> createFuture(): Future<T>
}

/**
 * The platform that the game is running on.
 */
lateinit var gamePlatform: GamePlatform
