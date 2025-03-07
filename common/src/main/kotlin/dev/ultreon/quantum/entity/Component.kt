package dev.ultreon.quantum.entity

import com.badlogic.gdx.utils.JsonValue

/**
 * Entity components are the building blocks of Quantum Voxel's entity system.
 * A component is a piece of data that an entity can have.
 * Components can be as simple as a name, or as complex as an inventory of items.
 * The components are managed by the entity manager, which is responsible for updating the components and handling
 * events.
 * Components are also used by the scripting system, which allows scripts to interact with the components
 * and the entity manager.
 *
 * Components are useful because they allow for a very flexible and extensible entity system.
 * They allow for
 * different types of entities to have different types of data, and are easily extensible.
 *
 * For example, a health component could be a component that an entity has, which represents the health of the
 * entity.
 * The health component could be updated by the entity manager when the entity is damaged or healed.
 *
 * ## Example usage:
 * ```kotlin
 * class HealthComponent : Component<HealthComponent> {
 *   var health: Int = 100
 *
 *   override fun json(): JsonValue {
 *     val json = JsonValue()
 *     json.put("health", health)
 *     return json
 *   }
 *
 *   override fun load(json: JsonValue) {
 *     health = json.getInt("health")
 *   }
 * }
 * ```
 *
 * @param T The type of the component.
 */
abstract class Component<T : Component<T>> : Comparable<Component<*>> {
  /**
   * The type of the component.
   */
  abstract val componentType: ComponentType<out T>

  override fun toString(): String {
    return "Component(${componentType.name})"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    other as Component<*>
    return componentType == other.componentType
  }

  override fun compareTo(other: Component<*>): Int {
    return componentType.compareTo(other.componentType)
  }

  override fun hashCode(): Int {
    return componentType.hashCode()
  }

  abstract fun json(): JsonValue
  abstract fun load(json: JsonValue)
}
