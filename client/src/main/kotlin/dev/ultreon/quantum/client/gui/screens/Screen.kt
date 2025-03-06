package dev.ultreon.quantum.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.client.guiScale
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.client.shapes
import ktx.app.KtxScreen
import ktx.graphics.color

private val background = color(0f, 0f, 0f, 0.5f)

/**
 * A screen in Quantum Voxel.
 * This is the base class for all screens in Quantum Voxel.
 *
 * ### Example usage:
 * ```kotlin
 * class MainMenuScreen : Screen() {
 *   override fun setup() {
 *     // Setup the screen
 *
 *     this.add<Label>() {
 *       text = "Quantum Voxel"
 *       size = 40f
 *     }
 *     this.add<Button>() {
 *       text = "Play"
 *       size = 20f
 *       onClick { quantum.showScreen(PlaceholderScreen) }
 *     }
 *     this.add<Button>() {
 *       text = "Settings"
 *       size = 20f
 *       onClick { quantum.showScreen(PlaceholderScreen) }
 *     }
 *     this.add<Button>() {
 *       text = "Exit"
 *       size = 20f
 *       onClick { quantum.exit() }
 *     }
 *   }
 *
 *   override fun render(delta: Float) {
 *     // Render the screen
 *
 *     if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
 *       quantum.showScreen(PlaceholderScreen)
 *     }
 *   }
 * }
 * ```
 *
 * @param parent The parent screen of this screen.
 * @constructor Creates a new Screen.
 * @author Qubilux
 */
abstract class Screen(parent: Screen? = null) : KtxScreen, GuiContainer(parent) {
  var title: String = ""

  /**
   * This function is called every frame and is responsible for rendering the screen.
   *
   * @param delta The time in seconds since the last frame.
   * @see KtxScreen.render
   */
  final override fun render(delta: Float) {
    id = "screen-${this::class.simpleName}"

    if (quantum.environmentRenderer != null) {
      if (this !is PlaceholderScreen) {
        @Suppress("RemoveRedundantQualifierName")
        PlaceholderScreen.render(delta)
        quantum.shapes
      }
    }

    if (!quantum.isTouch) {
      quantum.submit {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
          if (quantum.environmentRenderer != null) {
            quantum.showScreen(PlaceholderScreen)
            Gdx.input.setCursorPosition(Gdx.graphics.width / 2, Gdx.graphics.height / 2)
            Gdx.input.isCursorCatched = true
          }
        }
      }
    }

    if (this !is PlaceholderScreen) {
      shapes.filledRectangle(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(), background)
    }

    super<KtxScreen>.render(delta)

    this.preRender(this)
    this.render(
      renderer = QuantumVoxel.instance.guiRenderer,
      x = (Gdx.input.x / guiScale).toInt(),
      y = (Gdx.input.y / guiScale).toInt(),
      delta
    )
  }

  /**
   * This function is called every frame and is responsible for rendering the screen.
   *
   * @param renderer The renderer to render the screen with.
   * @param x The x position of the mouse.
   * @param y The y position of the mouse.
   * @param delta The time in seconds since the last frame.
   * @see GuiContainer.render
   */
  override fun render(renderer: GuiRenderer, x: Int, y: Int, delta: Float) {
    super<GuiContainer>.render(renderer, x, y, delta)
  }

  /**
   * This function is called when the screen is resized.
   *
   * @param width The new width of the screen.
   * @param height The new height of the screen.
   * @see KtxScreen.resize
   */
  final override fun resize(width: Int, height: Int) {
    super.resize(width, height)
    super.width = width.toFloat()
    super.height = height.toFloat()

    this.resized()
  }

  /**
   * This function is called when the screen is resized.
   *
   * @see KtxScreen.resize
   */
  open fun resized() {

  }

  /**
   * This function is called when the screen is shown.
   *
   * @see KtxScreen.show
   */
  override fun show() {
    super.show()

    this.width = Gdx.graphics.width.toFloat() / guiScale
    this.height = Gdx.graphics.height.toFloat() / guiScale

    setup()
  }

  /**
   * This function is called when the screen is hidden.
   */
  open fun Stage.init() = Unit

  /**
   * This function is called when the screen is hidden.
   */
  abstract fun setup()

  /**
   * This function is called when the screen is hidden.
   *
   * @see KtxScreen.dispose
   */
  override fun dispose() {
    super<KtxScreen>.dispose()
    super<GuiContainer>.dispose()
  }

  /**
   * This function is called when the screen is hidden.
   *
   * @see KtxScreen.hide
   */
  override fun hide() {

  }
}
