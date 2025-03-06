package dev.ultreon.quantum.client.input

import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import ktx.math.vec2

@Deprecated("No touch support anymore", level = DeprecationLevel.WARNING)
class TouchMovement(val touchpad: Touchpad?) : PlayerMovement {
  var up = false
  var down = false

  override var motionX = 0f
  override var motionZ = 0f

  override var movement = vec2()

  override fun update() {
    motionX = touchpad?.knobPercentX ?: 0F
    motionZ = touchpad?.knobPercentY ?: 0F

    movement.set(motionX, motionZ).nor()
  }

  override fun reset() {
    motionX = 0F
    motionZ = 0F
    up = false
    down = false
  }
}
