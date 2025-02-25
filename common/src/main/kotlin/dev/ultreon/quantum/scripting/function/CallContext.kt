package dev.ultreon.quantum.scripting.function

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.scripting.ContextAware
import dev.ultreon.quantum.scripting.ContextType
import dev.ultreon.quantum.scripting.ContextValue

class CallContext(val originJson: JsonValue = JsonValue(JsonValue.ValueType.nullValue)) {
  val paramValues = hashMapOf<String, ContextValue<*>>()

  operator fun <T : Any> set(type: ContextType<out T>, value: T) {
    paramValues[type.name] = ContextValue(type, value)
  }

  operator fun <T : Any> set(name: String, value: ContextValue<T>) {
    paramValues[name] = value
  }

  fun getString(name: String): String? {
    return paramValues[name]?.value as? String
  }

  fun getInt(name: String): Int? {
    return (paramValues[name]?.value as? Number)?.toInt()
  }

  fun getLong(name: String): Long? {
    return (paramValues[name]?.value as? Number)?.toLong()
  }

  fun getFloat(name: String): Float? {
    return (paramValues[name]?.value as? Number)?.toFloat()
  }

  fun getDouble(name: String): Double? {
    return (paramValues[name]?.value as? Number)?.toDouble()
  }

  fun getBoolean(name: String): Boolean? {
    return paramValues[name]?.value as? Boolean
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> get(contextType: ContextType<T>): ContextValue<T>? {
    return paramValues[contextType.name]?.value as ContextValue<T>?
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> getList(contextType: ContextType<T>): List<ContextValue<*>>? {
    return paramValues[contextType.name]?.value as? List<ContextValue<*>>
  }

  inline fun <reified T : ContextAware<T>> get(name: String): T? {
    return paramValues[name]?.value as? T
  }

  inline fun <reified T : ContextAware<T>> getList(name: String): List<T>? {
    return (paramValues[name]?.value as? List<*>)?.mapNotNull { it as? T? }
  }

  inline fun <reified T : Any> getNullableList(name: String): List<T?>? {
    return (paramValues[name]?.value as? List<*>)?.map { it as? T? }
  }

  fun getRaw(s: String): Any? {
    return paramValues[s]?.value
  }

  fun getValue(name: String): ContextValue<*>? {
    return paramValues[name]
  }

  fun getJson(s: String): JsonValue? {
    return paramValues[s]?.value as? JsonValue
  }

  companion object {
    fun from(value: JsonValue): CallContext? {
      val context = CallContext(value)
      if (value.isObject) {
        context.paramValues[value.getString("context-type", "###").also {
          if (it == "###") {
            logger.error("No context type specified: ${value.trace()}")
            return@from null
          }
        }] = ContextType[value.getString("context-type")]?.parse(value) as? ContextValue<*>? ?: run {
          logger.error("Unknown context type: ${value.getString("type")}")
          return null
        }
        return context
      }
      if (!value.isArray) {
        throw IllegalArgumentException("CallContext value must be an array")
      }
      value.forEach {
        context.paramValues[it.getString("type")] =
          ContextType[it.getString("type")]?.parse(it) as? ContextValue<*>? ?: run {
            logger.error("Unknown context type: ${it.getString("type")}")
            return null
          }
      }
      return context
    }
    fun of(json: JsonValue, vararg contextType: ContextValue<*>): CallContext? {
      val context = CallContext(json)
      contextType.forEach {
        context.paramValues[it.type.name] = it
      }
      return context
    }
  }
}
