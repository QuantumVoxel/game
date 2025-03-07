package dev.ultreon.quantum.client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.screens.IdScreen
import dev.ultreon.quantum.client.gui.screens.PlaceholderScreen
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.client.input.ControllerMovement
import dev.ultreon.quantum.client.input.KeyMovement
import dev.ultreon.quantum.client.input.TouchMovement
import dev.ultreon.quantum.client.model.ModelRegistry
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.util.id

/**
 * The loading screen of the game.
 * This screen is shown when the game is loading.
 *
 * @see QuantumVoxel.loaded
 * @author Qubilux
 * @constructor Creates a new LoadScreen.
 */
class LoadScreen : Screen() {
  private var loaded: Boolean = false
  private var crash: Array<StackTraceElement>? = null
  private val batch: SpriteBatch = quantum.globalBatch
  private var t: Thread? = null
  private var message = ""
    set(value) {
      field = value

      logger.info("Loading stage: $value")
    }

  /**
   * This function is called when the screen is shown.
   */
  override fun show() {
    if (t != null) {
      logger.warn("LoadScreen was already initialized!")
      return
    }
    t = Thread {
      try {
        message = "Loading textures..."
        textureManager.init()
        message = "Loading blocks textures..."
        textureManager.registerAtlas("blocks")
        message = "Loading item textures..."
        textureManager.registerAtlas("font")
        message = "Loading GUI textures..."
        textureManager.registerAtlas("gui")

        message = "Packing textures..."
        textureManager.pack()

        message = "Loading models..."
        ModelRegistry.loadModels()

        message = "Initializing..."
        QuantumVoxel {
          quantum.keyMovement = KeyMovement()
          quantum.controllerMovement = ControllerMovement()

          IdScreen.load(quantum.clientResources)

          quantum.showScreen(TitleScreen() ?: run {
            logger.error("Title screen not found")
            PlaceholderScreen
          })

          message = "Done!"

          this@LoadScreen.loaded = true
        }
      } catch (e: Throwable) {
        e.printStackTrace()
        crash = e.stackTrace
      }
    }.apply {
      start()
    }

    super.show()
  }

  /**
   * Sets up the screen (or re-sets it up).
   * This function is called when the screen is shown or resized.
   */
  override fun setup() {
    batch.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
  }

  /**
   * This function is called every frame and is responsible for rendering the widget.
   *
   * @param renderer The renderer to render the widget with.
   * @param mouseX The x position of the mouse.
   * @param mouseY The y position of the mouse.
   * @param delta The time in seconds since the last frame.
   */
  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    if (crash != null) {
      var y = 0
      for (stackTraceElement in crash!!) {
        y += quantum.bitmapFont.lineHeight.toInt()
        val fileName = stackTraceElement.fileName
        if (fileName != null) {
          quantum.bitmapFont.draw(
            batch,
            stackTraceElement.className.replace(
              "/",
              "."
            ) + "." + stackTraceElement.methodName + " (" + fileName.substring(
              fileName.lastIndexOf("/") + 1
            ) + ":" + stackTraceElement.lineNumber + ")",
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height / 2f - y.toFloat()
          )
        } else {
          quantum.bitmapFont.draw(
            batch,
            stackTraceElement.className.replace(
              "/",
              "."
            ) + "." + stackTraceElement.methodName + " (<Unknown File>:" + stackTraceElement.lineNumber + ")",
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height / 2f - y.toFloat()
          )
        }
      }
    }

    if (t?.isAlive != false) {
      quantum.bitmapFont.draw(batch, "Loading...", Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
      quantum.bitmapFont.draw(batch, message, Gdx.graphics.width / 2f, Gdx.graphics.height / 2f - 20f)
    }
  }
}
