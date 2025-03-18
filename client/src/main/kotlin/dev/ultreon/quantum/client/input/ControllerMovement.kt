package dev.ultreon.quantum.client.input

//import com.badlogic.gdx.controllers.Controllers
import dev.ultreon.quantum.ExperimentalApi
import ktx.math.vec2

/**
 * Controller movement for the player.
 *
 * @constructor Creates a new controller movement.
 *
 * @property up Whether the player is moving up.
 * @property down Whether the player is moving down.
 * @property motionX The horizontal x-axis movement amount for the player.
 * @property motionZ The horizontal z-axis movement amount for the player.
 * @property movement The movement vector of the player.
 */
@ExperimentalApi
class ControllerMovement() : PlayerMovement {
  var up = false
  var down = false

  override var motionX = 0f
  override var motionZ = 0f

  override var movement = vec2()

  override fun update() {
//    val current = Controllers.getCurrent()
//    val mapping = current.mapping
//    val axisLeftX = mapping.axisLeftX
//    val axisLeftY = mapping.axisLeftY
//    motionX = current.getAxis(axisLeftX)
//    motionZ = current.getAxis(axisLeftY)

    movement.set(motionX, motionZ).nor()
  }

  override fun reset() {
    motionX = 0f
    motionZ = 0f
    up = false
    down = false
  }
}
