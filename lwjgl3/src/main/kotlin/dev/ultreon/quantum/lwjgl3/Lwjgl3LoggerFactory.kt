package dev.ultreon.quantum.lwjgl3

import dev.ultreon.quantum.Logger
import dev.ultreon.quantum.LoggerFactory
import java.lang.ref.Cleaner

private val cleaner = Cleaner.create()

object Lwjgl3LoggerFactory : LoggerFactory {
  private val loggers = mutableMapOf<String, Lwjgl3Logger>()

  override fun getLogger(name: String): Logger {
    synchronized(loggers) {
      if (name in loggers) {
        return loggers[name]!!
      }

      val logger = Lwjgl3Logger(name)
      loggers[name] = logger
      return logger
    }
  }

  init {
    cleaner.register(this) {
      for (logger in loggers.values) {
        logger.close()
      }
      loggers.clear()
    }
  }
}
