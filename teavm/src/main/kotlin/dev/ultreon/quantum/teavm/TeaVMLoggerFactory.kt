package dev.ultreon.quantum.teavm

import dev.ultreon.quantum.Logger
import dev.ultreon.quantum.LoggerFactory

class TeaVMLoggerFactory : LoggerFactory {
  override fun getLogger(name: String): Logger = TeaVMLogger(name)
}

