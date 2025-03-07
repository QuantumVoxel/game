package dev.ultreon.quantum

import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.network.BaseSocket
import dev.ultreon.quantum.resource.ResourceManager

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

  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isAndroid: Boolean get() = false

  @Deprecated("Not used anymore", ReplaceWith("false"))
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
  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isMobile: Boolean get() = false

  /**
   * Handles next-frame logic. Useful for when a platform doesn't do something every frame that it should.
   */
  @Deprecated("Not used anymore")
  fun nextFrame() {

  }

  @Deprecated("Not used anymore")
  fun createClientSocket(asString: String): BaseSocket? {
    return null
  }

  /**
   * Returns `true` if the game is running on WebGL 3.0.
   */
  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isWebGL3: Boolean get() = false

  /**
   * Returns `true` if the game is running on WebGL 2.0.
   */
  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isWebGL2: Boolean get() = false

  /**
   * Returns `true` if the game is running using SwitchGDX backend.
   */
  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isSwitchGDX: Boolean get() = false

  /**
   * Returns `true` if the game is running on Switch.
   */
  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isSwitch: Boolean get() = false

  /**
   * Returns `true` if the game is running on UWP.
   */
  @Deprecated("Not used anymore", ReplaceWith("false"))
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
  @Deprecated("Not used anymore", ReplaceWith("false"))
  val isWeb: Boolean get() = false

  /**
   * Terminates the game immediately.
   *
   * @param i the exit status
   */
  @Deprecated("Not used anymore", ReplaceWith("Runtime.getRuntime().halt(i)", "java.lang.Runtime"))
  fun halt(i: Int)

  /**
   * Returns the number of CPU cores the device has.
   *
   * @return the number of CPU cores
   */
  @Deprecated("Not used anymore", ReplaceWith("Runtime.getRuntime().availableProcessors()", "java.lang.Runtime"))
  fun cpuCores(): Int

  /**
   * Yields the current thread.
   */
  @Deprecated("Not used anymore", ReplaceWith("Thread.yield()", "java.lang.Thread"))
  fun yield()

  /**
   * Creates an [AsyncExecutor] with the specified number of max concurrent threads.
   *
   * @param maxConcurrent the max number of concurrent threads
   * @param name the name of the thread
   * @return the created [AsyncExecutor]
   */
  @Deprecated("Not used anymore", ReplaceWith("AsyncExecutor(maxConcurrent, name)", "com.badlogic.gdx.utils.async.AsyncExecutor"))
  fun createAsyncExecutor(maxConcurrent: Int, name: String = "AsyncExecutor-Thread"): AsyncExecutor

  /**
   * Pauses the current thread for a specified duration.
   *
   * @param i the duration in milliseconds to sleep
   */
  @Deprecated("Not used anymore")
  fun sleep(i: Int)

  /**
   * Creates a new instance of [Future].
   *
   * @return a new [Future] instance
   */
  @Deprecated("Not used anymore", ReplaceWith("CompletableFuture()", "java.util.concurrent.CompletableFuture"))
  fun <T> createFuture(): Future<T>
}

/**
 * The platform that the game is running on.
 */
lateinit var gamePlatform: GamePlatform
