package dev.ultreon.quantum.entity

import com.badlogic.gdx.graphics.g3d.model.Animation
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.scripting.`null`

class AnimationComponent() : Component<AnimationComponent>() {
  var animation: Animation? = null
  var animationController: AnimationController? = null

  override val componentType = ComponentType.animation
  override fun json(): JsonValue = `null`()
  override fun load(json: JsonValue) = Unit

  fun update(delta: Float) {
    animationController?.update(delta)
  }
}
