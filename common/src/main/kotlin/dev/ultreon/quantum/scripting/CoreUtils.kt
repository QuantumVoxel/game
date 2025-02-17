package dev.ultreon.quantum.scripting

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.commonResources
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.scripting.function.function

object CoreUtils : ContextAware<CoreUtils> {
  private val logInfo = function(
    ContextParam("message", ContextType.string),
    function = {
      logger.info(it.getString("message") ?: "null")
      null
    }
  )

  private val logWarn = function(
    ContextParam("message", ContextType.string),
    function = {
      logger.warn(it.getString("message") ?: "null")
      null
    }
  )

  private val logError = function(
    ContextParam("message", ContextType.string),
    function = {
      logger.error(it.getString("message") ?: "null")
      null
    }
  )

  private val log = logInfo
  override val persistentData: PersistentData = PersistentData()

  override fun contextType(): ContextType<CoreUtils> {
    return ContextType.core
  }

  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "log_info" -> ContextValue(ContextType.function, logInfo)
      "log_warn" -> ContextValue(ContextType.function, logWarn)
      "log_error" -> ContextValue(ContextType.function, logError)
      "log" -> ContextValue(ContextType.function, log)
      "resources" -> ContextValue(ContextType.resources, commonResources)
      "math" -> ContextValue(ContextType.math, MathUtils)
      "platform" -> ContextValue(ContextType.platform, PlatformUtils)
      "files" -> ContextValue(ContextType.files, FilesUtils)
      else -> null
    }
  }
}
