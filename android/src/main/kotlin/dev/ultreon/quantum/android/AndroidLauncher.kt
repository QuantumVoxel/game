package dev.ultreon.quantum.android

import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy
import dev.ultreon.quantum.BuildConfig
import dev.ultreon.quantum.GamePlatform
import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.client.gameInput
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.resource.ResourceManager
import kotlin.system.exitProcess


/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    gamePlatform = object : GamePlatform {
      override fun loadResources(resourceManager: ResourceManager) {
        resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"))
      }

      override val isMobile: Boolean
        get() = true

      override val isDebug: Boolean
        get() = BuildConfig.DEBUG

      override fun cpuCores(): Int {
        return Runtime.getRuntime().availableProcessors()
      }

      override val isAndroid: Boolean
        get() = true

      override fun yield() {
        Thread.yield()
      }

      override fun createAsyncExecutor(maxConcurrent: Int, name: String): AsyncExecutor {
        return AndroidAsyncExecutor(maxConcurrent, name)
      }

      override fun sleep(i: Int) {
        Thread.sleep(i.toLong())
      }

      override fun <T> createFuture(): Future<T> {
        return AndroidFuture()
      }

      override fun halt(i: Int) {
        exitProcess(i)
      }

      override val isGLES3: Boolean
        get() = Gdx.gl30 != null

      override val isGLES2: Boolean
        get() = Gdx.gl20 != null

      override val isServer: Boolean
        get() = false
    }
    initialize(QuantumVoxel(), AndroidApplicationConfiguration().apply {
      // Configure your application here.
      useImmersiveMode = true // Recommended, but not required.
      useGyroscope = true
      useCompass = true
      useAccelerometer = true
      useGL30 = true
      r = 8
      g = 8
      b = 8
      a = 8
      depth = 24
      stencil = 8
    })
  }

  override fun onGenericMotionEvent(event: MotionEvent): Boolean {
    if ((event.source and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
      if (event.actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
        gameInput.onMouseMove(event.x, event.y) // Handle the movement

        // Reset the cursor to the center
        resetCursorToCenter()
      }
      return true
    }
    return super.onGenericMotionEvent(event)
  }

  private fun resetCursorToCenter() {
    val centerEvent = MotionEvent.obtain(
      System.currentTimeMillis(),
      System.currentTimeMillis(),
      MotionEvent.ACTION_HOVER_MOVE,
      Gdx.graphics.width / 2f,
      Gdx.graphics.height / 2f,
      0
    )
    super.onGenericMotionEvent(centerEvent)
    centerEvent.recycle()
  }
}
