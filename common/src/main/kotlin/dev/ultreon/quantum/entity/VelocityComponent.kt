package dev.ultreon.quantum.entity

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.scripting.`null`

class VelocityComponent : Component<VelocityComponent>() {
  var x = 0f
  var y = 0f
  var z = 0f

  override val componentType = ComponentType.velocity
  override fun json(): JsonValue {
    return `null`()
  }

  override fun load(json: JsonValue) {

  }
}
