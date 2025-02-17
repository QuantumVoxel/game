package dev.ultreon.quantum.teavm

import dev.ultreon.quantum.Logger
import org.teavm.jso.JSObject

class TeaVMLogger(val name: String) : Logger, JSObject {
  override fun info(message: String) {
    TeaVMConsole.info("$name: $message")
  }

  override fun warn(message: String) {
    TeaVMConsole.warn("$name: $message")
  }

  override fun error(message: String) {
    TeaVMConsole.error("$name: $message")
  }

  override fun debug(message: String) {
    TeaVMConsole.debug("$name: $message")
  }

  override fun trace(message: String) {
    TeaVMConsole.debug("$name (trace): $message")
  }

  override fun info(message: String, obj: Any?) {
    TeaVMConsole.info("$name: $message", obj)
  }

  override fun warn(message: String, obj: Any?) {
    TeaVMConsole.warn("$name: $message", obj)
  }

  override fun error(message: String, obj: Any?) {
    TeaVMConsole.error("$name: $message", obj)
  }

  override fun debug(message: String, obj: Any?) {
    TeaVMConsole.debug("$name: $message", obj)
  }

  override fun trace(message: String, obj: Any?) {
    TeaVMConsole.debug("$name (trace): $message", obj)
  }
}
