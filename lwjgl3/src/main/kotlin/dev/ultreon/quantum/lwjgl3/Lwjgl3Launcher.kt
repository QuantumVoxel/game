@file:JvmName("Lwjgl3Launcher")

package dev.ultreon.quantum.lwjgl3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Os
import com.badlogic.gdx.utils.SharedLibraryLoader
import dev.ultreon.quantum.*
import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.client.*
import dev.ultreon.quantum.resource.ResourceManager
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application as OpenGLApp
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration as OpenGLConfig

/** Launches the desktop (LWJGL3) application. */
fun main() {
  factory = Lwjgl3LoggerFactory

  val logger = LoggerFactory["Lwjgl3Launcher"]

  try {
    ANSI.enableWindowsAnsi()
  } catch (e: Throwable) {
    logger.error("Failed to enable ANSI support: ${e.message}")
  }

  // Extract mac64/*.dylib or macarm64/*.dylib into the same directory as where it ran from
  val osName = System.getProperty("os.name").lowercase()
  if (osName.contains("mac", ignoreCase = true)) {
    val archName = System.getProperty("os.arch").lowercase()
    if (archName.contains("aarch64", ignoreCase = true)) {
      Lwjgl3Logger::class.java.getResourceAsStream("/macarm64/libEGL.dylib")?.use { input ->
        Files.copy(input, Path("./libEGL.dylib"), StandardCopyOption.REPLACE_EXISTING)
      }
      Lwjgl3Logger::class.java.getResourceAsStream("/macarm64/libGLESv2.dylib")?.use { input ->
        Files.copy(input, Path("./libGLESv2.dylib"), StandardCopyOption.REPLACE_EXISTING)
      }
    } else {
      Lwjgl3Logger::class.java.getResourceAsStream("/mac64/libEGL.dylib")?.use { input ->
        Files.copy(input, Path("./libEGL.dylib"), StandardCopyOption.REPLACE_EXISTING)
      }
      Lwjgl3Logger::class.java.getResourceAsStream("/mac64/libGLESv2.dylib")?.use { input ->
        Files.copy(input, Path("./libGLESv2.dylib"), StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }

  // This handles macOS support and helps on Windows.
  if (StartupHelper.startNewJvmIfRequired())
    return

  try {
    when (SharedLibraryLoader.os) {
//      Os.MacOsX -> {
//        MetalApp(quantum, MetalConfig().apply {
//          setTitle("Quantum Voxel")
//          setWindowedMode(MINIMUM_WIDTH * 2, MINIMUM_HEIGHT * 2)
//          setOpenGLEmulation(MetalConfig.GLEmulation.ANGLE_GLES32, 2, 0)
//          setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
//          setBackBufferConfig(4, 4, 4, 4, 8, 8, 0)
//        })
//      }

      Os.Windows -> {
        gamePlatform = OpenGLPlatform(logger)

        OpenGLApp(QuantumVoxel(), OpenGLConfig().apply {
          setTitle("Quantum Voxel")
          val monitor = OpenGLConfig.getPrimaryMonitor().let { primaryMonitor ->
            val primaryMode = OpenGLConfig.getDisplayMode(primaryMonitor)
            if (primaryMode.width - 64 < MINIMUM_WIDTH * 3 - 2 && primaryMode.height - 64 < MINIMUM_HEIGHT * 3 - 2) {
              return@let OpenGLConfig.getMonitors().asSequence().sortedByDescending { it.virtualX }.firstOrNull {
                val displayMode = OpenGLConfig.getDisplayMode(it)
                displayMode.width - 64 < MINIMUM_WIDTH * 3 - 2 && displayMode.height - 64 < MINIMUM_HEIGHT * 3 - 2
              } ?: run { primaryMonitor }
            }

            return@let primaryMonitor
          }

          val displayMode = OpenGLConfig.getDisplayMode(monitor)
//          if (displayMode.width - 64 < MINIMUM_WIDTH * 3 - 2 && displayMode.height - 64 < MINIMUM_HEIGHT * 3 - 2) {
//            setWindowedMode(MINIMUM_WIDTH * 3 - 2, MINIMUM_HEIGHT * 3 - 2)
//          } else {
//            setWindowedMode(MINIMUM_WIDTH * 2, MINIMUM_HEIGHT * 2)
//          }
          setMaximized(true)
          setMaximizedMonitor(monitor)
          setForegroundFPS(0)
          useVsync(false)
          setOpenGLEmulation(OpenGLConfig.GLEmulation.GL32, 3, 2)
          setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
          setBackBufferConfig(4, 4, 4, 4, 8, 8, 0)
        })
      }

      Os.MacOsX -> {
        gamePlatform = object : OpenGLPlatform(logger) {
          override val isGL32: Boolean
            get() = false

          override val isGL20: Boolean
            get() = true
        }

        OpenGLApp(QuantumVoxel(), OpenGLConfig().apply {
          setTitle("Quantum Voxel")
          setWindowedMode(MINIMUM_WIDTH * 3 - 2, MINIMUM_HEIGHT * 3 - 2)
          setForegroundFPS(0)
          useVsync(false)
          setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
          setBackBufferConfig(4, 4, 4, 4, 8, 8, 0)
        })
      }

      else -> {
        gamePlatform = OpenGLPlatform(logger)

        OpenGLApp(QuantumVoxel(), OpenGLConfig().apply {
          setTitle("Quantum Voxel")
          setWindowedMode(MINIMUM_WIDTH * 3 - 2, MINIMUM_HEIGHT * 3 - 2)
          setForegroundFPS(0)
          useVsync(false)
          setOpenGLEmulation(OpenGLConfig.GLEmulation.GL32, 3, 2)
          setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
          setBackBufferConfig(4, 4, 4, 4, 8, 8, 0)
        })
      }
    }
  } catch (e: Throwable) {
    logger.error("Failed to create Quantum Voxel:\n${e.stackTraceToString()}")
  }
}

abstract class DesktopPlatform(val logger: Logger) : GamePlatform {
  override fun loadResources(resourceManager: ResourceManager) {
//      try {
//        // Locate resource "_assetroot" and use its parent directory as the root
//        val resource = QuantumVoxel::class.java.classLoader.getResource("_assetroot")
//        val path = resource?.toURI()?.toPath()?.parent ?: throw FileNotFoundException("Asset root not found")
//        resourceManager.load(Gdx.files.absolute(path.toString()))
//
//      } catch (e: Exception) {
//        ZipInputStream(QuantumVoxel::class.java.getResourceAsStream("/quantum.zip")?.buffered()
//                ?: Files.newInputStream(Paths.get("quantum.zip")).buffered()).use {
//          resourceManager.loadZip(it)
//        }
//      }

    resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"))
  }

  override val isDesktop: Boolean
    get() = true

  override val isWindows: Boolean
    get() = SharedLibraryLoader.os == Os.Windows

  override val isLinux: Boolean
    get() = SharedLibraryLoader.os == Os.Linux

  override val isMac: Boolean
    get() = SharedLibraryLoader.os == Os.MacOsX

  override val isDebug: Boolean
    get() = ManagementFactory.getRuntimeMXBean().inputArguments.any { "jdwp" in it || "-Xdebug" in it || "-Xrunjdwp" in it }

  override fun cpuCores(): Int {
    return Runtime.getRuntime().availableProcessors()
  }

  override fun yield() {
    Thread.yield()
  }

  override fun sleep(i: Int) {
    Thread.sleep(i.toLong())
  }

  override fun halt(i: Int) {
    Runtime.getRuntime().halt(i)
  }

  override val isMobile: Boolean
    get() = false

  override fun dispose() {
    logger.info("Exiting...")

    // Loop through threads and interrupt them, unless they are the main thread
    Thread.getAllStackTraces().keys.filter { it != Thread.currentThread() && it.isAlive && !it.isDaemon && it.name != "Finalizer" }
      .forEach {
        if (it.id == Thread.currentThread().id) return@forEach
        logger.warn("Interrupting thread ${it.name} due to it being stuck")
        it.interrupt()
        it.join(1000)
        if (it.isAlive) {
          logger.error("Thread ${it.name} is still running! Halting JVM...")
          Runtime.getRuntime().halt(1)
        }
      }
  }

  override fun createAsyncExecutor(maxConcurrent: Int, name: String): AsyncExecutor {
    return Lwjgl3AsyncExecutor(maxConcurrent, name)
  }

  override fun <T> createFuture(): Future<T> {
    return Lwjgl3Future()
  }
}

open class VulkanPlatform(logger: Logger) : DesktopPlatform(logger) {
  override val isGL32: Boolean
    get() = false

  override val isGLES3: Boolean
    get() = true
}

open class OpenGLPlatform(logger: Logger) : DesktopPlatform(logger) {
  override val isGL32: Boolean
    get() = true

  override val isGLES2: Boolean
    get() = false
}

open class MetalPlatform(logger: Logger) : DesktopPlatform(logger) {
  override val isGL32: Boolean
    get() = false

  override val isGLES2: Boolean
    get() = true
}
