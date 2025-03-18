package dev.ultreon.quantum.entity

import com.badlogic.gdx.utils.JsonValue

/**
 * Represents a player component.
 * @param name The name of the player.
 * @constructor Creates a new player component.
 * @param T The type of the player component.
 *
 * @property name The name of the player.
 */
abstract class PlayerComponent<T : PlayerComponent<T>>(val name: String = "Player") : Component<T>() {
  override fun json(): JsonValue {
    return JsonValue(JsonValue.ValueType.`object`).also { json ->
      // No-op
    }
  }

  override fun load(json: JsonValue) {

  }
}
