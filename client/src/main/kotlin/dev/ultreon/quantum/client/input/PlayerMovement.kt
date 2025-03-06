package dev.ultreon.quantum.client.input

import com.badlogic.gdx.math.Vector2

/**
 * Represents the movement of the player.
 *
 * @property movement The movement vector of the player.
 * @property motionX The horizontal x-axis movement amount for the player.
 * @property motionZ The horizontal z-axis movement amount for the player.
 */
interface PlayerMovement {
  var movement: Vector2
  var motionX: Float
  var motionZ: Float

  /**
   * Updates the player movement.
   */
  fun update()

  /**
   * Resets the player movement.
   */
  fun reset()
}
