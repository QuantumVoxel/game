package dev.ultreon.quantum.scripting

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.scripting.function.CallContext

/**
 * This interface is used to represent an object that can be used in a scripting context.
 * This is used to allow for the creation of custom objects that can be used in scripts.
 *
 * @param T The type of the object that is being represented.
 *
 * @property persistentData The persistent data of the object.
 *
 * @see ContextType
 * @see ContextValue
 */
interface ContextAware<T : ContextAware<T>> {
  val persistentData: PersistentData

  /**
   * Gets the context type of the object.
   *
   * @return The context type of the object.
   */
  fun contextType(): ContextType<T>

  /**
   * Gets the supported types of the object.
   *
   * @return The supported types of the object.
   */
  fun supportedTypes(): List<ContextType<*>> = listOf(contextType())

  /**
   * Gets the field of the object with the specified name.
   *
   * @param name The name of the field.
   * @param contextJson The context JSON of the object.
   *
   * @return The field of the object with the specified name.
   */
  fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? = null

  @Deprecated("Not used anymore")
  fun setArg(param: ContextParam<*>, callContext: CallContext) {

  }
}
