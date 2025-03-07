package dev.ultreon.quantum.client.input

import com.badlogic.gdx.math.Vector2

/**
 * Represents the input of the game, including mouse and keyboard input.
 */
open class GameInput {
  private val mousePosition = Vector2()
  private val mouseDelta = Vector2()
  private val oldMousePosition = Vector2()
  private val tmp = Vector2()

  /**
   * Called when the mouse is moved.
   */
  fun onMouseMove(x: Float, y: Float) {
    mousePosition.set(x, y)
  }

  /**
   * Updates the mouse delta and old mouse position.
   */
  fun update() {
    tmp.set(mousePosition)
    mouseDelta.set(tmp).sub(oldMousePosition)
    oldMousePosition.set(tmp)
  }

  /**
   * Returns the current mouse position.
   */
  val mouseX: Float get() = mousePosition.x

  /**
   * Returns the current mouse position.
   */
  val mouseY: Float get() = mousePosition.y

  /**
   * Returns the current mouse delta.
   */
  val mouseDeltaX: Float get() = mouseDelta.x

  /**
   * Returns the current mouse delta.
   */
  val mouseDeltaY: Float get() = mouseDelta.y

  /**
   * Returns whether the mouse is supported.
   */
  val isMouseSupported: Boolean get() = false
}
