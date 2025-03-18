package dev.ultreon.quantum.client.input

import ktx.math.vec2

/**
 * Represents the movement of the player using the keyboard.
 *
 * @constructor Creates a new key movement.
 *
 * @property forward Whether the player is moving forward.
 * @property backward Whether the player is moving backward.
 * @property strafeLeft Whether the player is strafing left.
 * @property strafeRight Whether the player is strafing right.
 * @property up Whether the player is moving up.
 * @property down Whether the player is moving down.
 * @property motionX The horizontal x-axis movement amount for the player.
 * @property motionZ The horizontal z-axis movement amount for the player.
 * @property movement The movement vector of the player.
 */
class KeyMovement : PlayerMovement {
  var forward = false
  var backward = false
  var strafeLeft = false
  var strafeRight = false
  var up = false
  var down = false

  override var motionX = 0f
  override var motionZ = 0f

  override var movement = vec2()

  /**
   * Updates the player movement with keyboard inputs.
   */
  override fun update() {
    forward = KeyBinds.walkForwardsKey.isJustPressed()
    backward = KeyBinds.walkBackwardsKey.isJustPressed()
    strafeLeft = KeyBinds.walkLeftKey.isJustPressed()
    strafeRight = KeyBinds.walkRightKey.isJustPressed()
    up = KeyBinds.jumpKey.isJustPressed()
    down = KeyBinds.crouchKey.isJustPressed()

    motionX = 0f
    motionZ = 0f
    if (forward) motionZ -= 1f
    if (backward) motionZ += 1f
    if (strafeLeft) motionX -= 1f
    if (strafeRight) motionX += 1f

    movement.set(motionX, motionZ).nor()
  }

  /**
   * Resets the player movement.
   */
  override fun reset() {
    forward = false
    backward = false
    strafeLeft = false
    strafeRight = false
    up = false
    down = false

    motionX = 0f
    motionZ = 0f
  }
}
