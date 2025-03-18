package dev.ultreon.quantum.client

import com.badlogic.gdx.Screen
import dev.ultreon.quantum.client.ScreenFactory.Companion.newScreen

/**
 * Implementation of a screen created with a [ScreenFactory].
 *
 * @param screenFactory The screen factory.
 * @constructor Creates a new ScreenImpl.
 */
class ScreenImpl(private val screenFactory: ScreenFactory) : Screen {
  override fun show() {
    screenFactory.onShow()
  }

  override fun render(delta: Float) {
    screenFactory.onRender(delta)
  }

  override fun resize(width: Int, height: Int) {
    screenFactory.onResize(width, height)
  }

  override fun pause() {
    screenFactory.onPause()
  }

  override fun resume() {
    screenFactory.onResume()
  }

  override fun hide() {
    screenFactory.onHide()
  }

  override fun dispose() {
    screenFactory.onDispose()
  }
}

/**
 * A factory for creating screens.
 *
 * @constructor Creates a new ScreenFactory.
 * @property onShow Called when the screen is shown.
 * @property onRender Called when the screen is rendered.
 * @property onResize Called when the screen is resized.
 * @property onPause Called when the screen is paused.
 * @property onResume Called when the screen is resumed.
 * @property onHide Called when the screen is hidden.
 * @property onDispose Called when the screen is disposed.
 */
class ScreenFactory {
  private var screen: ScreenImpl? = ScreenImpl(this)

  @JvmField
  var onShow: () -> Unit = {}
  @JvmField
  var onRender: (delta: Float) -> Unit = {}
  @JvmField
  var onResize: (width: Int, height: Int) -> Unit = { _, _ -> }
  @JvmField
  var onPause: () -> Unit = {}
  @JvmField
  var onResume: () -> Unit = {}
  @JvmField
  var onHide: () -> Unit = {}
  @JvmField
  var onDispose: () -> Unit = {}

  /**
   * Sets the function to be called when the screen is shown.
   *
   * @property newScreen The function to be called when the screen is shown.
   */
  companion object {
    /**
     * Creates a new screen factory.
     *
     * @return A new screen factory.
     */
    @JvmStatic
    @Deprecated("Use IdScreen or Screen directly instead")
    fun newScreen(): ScreenFactory = ScreenFactory()
  }
}
