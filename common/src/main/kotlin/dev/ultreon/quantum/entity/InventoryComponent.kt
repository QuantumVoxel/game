package dev.ultreon.quantum.entity

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.scripting.`null`

class InventoryComponent : Component<InventoryComponent>() {
  var hotbarIndex: Int = 0
  private val slots = Array(30) { ItemStack() }
  private val hotbar = Array(10) { ItemStack() }
  override val componentType = ComponentType.inventory
  override fun json(): JsonValue {
    return JsonValue(JsonValue.ValueType.`object`).also { json ->
      json.addChild("hotbarIndex", JsonValue(hotbarIndex.toLong()))
      json.addChild("slots", slots.map { it.json() }.`null`())
      json.addChild("hotbar", hotbar.map { it.json() }.`null`())
    }
  }

  override fun load(json: JsonValue) {
    hotbarIndex = json["hotbarIndex"]?.asInt() ?: 0
    slots.forEachIndexed { index, _ -> slots[index] = ItemStack(json["slots"][index]) }
    hotbar.forEachIndexed { index, _ -> hotbar[index] = ItemStack(json["hotbar"][index]) }
  }
}
