package dev.ultreon.quantum

import com.badlogic.gdx.Gdx

/**
 * A simple logging interface
 * This is used to allow for the creation of custom loggers.
 *
 * @see LoggerFactory
 */
interface Logger {
  /**
   * Log a message at info level.
   *
   * @param message message to log
   */
  fun info(message: String)

  /**
   * Log a message at warn level.
   *
   * @param message message to log
   */
  fun warn(message: String)

  /**
   * Log a message at error level.
   *
   * @param message message to log
   */
  fun error(message: String)

  /**
   * Log a message at debug level.
   *
   * @param message message to log
   */
  fun debug(message: String)

  /**
   * Log a message at trace level.
   *
   * @param message message to log
   */
  fun trace(message: String)

  /**
   * Log a message at info level with an object.
   *
   * @param message message to log
   * @param obj     object to log
   */
  fun info(message: String, obj: Any?) {
    info("$message :: $obj")
  }

  /**
   * Log a message at warn level with an object.
   *
   * @param message message to log
   * @param obj     object to log
   */
  fun warn(message: String, obj: Any?) {
    warn("$message :: $obj")
  }

  /**
   * Log a message at error level with an object.
   *
   * @param message message to log
   * @param obj     object to log
   */
  fun error(message: String, obj: Any?) {
    error("$message :: $obj")
  }

  /**
   * Log a message at debug level with an object.
   *
   * @param message message to log
   * @param obj     object to log
   */
  fun debug(message: String, obj: Any?) {
    debug("$message :: $obj")
  }

  /**
   * Log a message at trace level with an object.
   *
   * @param message message to log
   * @param obj     object to log
   */
  fun trace(message: String, obj: Any?) {
    trace("$message :: $obj")
  }
}

var factory = LoggerFactory {
  object : Logger {
    override fun info(message: String) {
      Gdx.app.applicationLogger.log(it, "[INFO] $message")
    }

    override fun warn(message: String) {
      Gdx.app.applicationLogger.error(it, "[WARN] $message")
    }

    override fun error(message: String) {
      Gdx.app.applicationLogger.error(it, "[ERROR] $message")
    }

    override fun debug(message: String) {
      Gdx.app.applicationLogger.log(it, "[DEBUG] $message")
    }

    override fun trace(message: String) {
      Gdx.app.applicationLogger.debug(it, "[TRACE] $message")
    }
  }
}
/**
 * A factory interface for creating loggers with specified names.
 */
fun interface LoggerFactory {
  /**
   * Retrieves a logger with the given name.
   *
   * @param name The name of the logger.
   * @return The logger instance.
   */
  fun getLogger(name: String): Logger

  companion object {
    /**
     * Provides access to a logger with the specified name.
     *
     * @param name The name of the logger.
     * @return The logger instance.
     */
    operator fun get(name: String): Logger {
      return factory.getLogger(name)
    }
  }
}
fun main() {
  val logger = LoggerFactory["Quantum"]
  logger.info("Quantum is running!")
  logger.warn("Quantum is running!")
  logger.error("Quantum is running!")
  logger.debug("Quantum is running!")
  logger.trace("Quantum is running!")

  val logger2 = LoggerFactory["Proton"]
  logger2.info("Proton is running!")

  val logger3 = LoggerFactory["Electron"]
  logger3.info("Electron is running!")

  val logger5 = LoggerFactory["Really long name that doesn't fit"]
  logger5.info("Really long name that doesn't fit is running!")

  val logger6 = LoggerFactory["ExtraLongNameWithoutSpaces"]
  logger6.info("ExtraLongNameWithoutSpaces is running!")
}
