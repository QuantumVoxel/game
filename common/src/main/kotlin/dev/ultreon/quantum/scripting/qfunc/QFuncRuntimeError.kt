package dev.ultreon.quantum.scripting.qfunc

import java.io.PrintStream
import java.io.PrintWriter

@Suppress("MemberVisibilityCanBePrivate")
class QFuncRuntimeError(override val message: String, val backtrace: List<BacktraceElement>) : RuntimeException() {
  override fun printStackTrace(s: PrintStream?) {
    s?.println("QFunc Backtrace:" + backtrace.joinToString("\n").prependIndent("  ") + "\nRuntime error: $message")
  }

  override fun printStackTrace(s: PrintWriter?) {
    s?.println("QFunc Backtrace:" + backtrace.joinToString("\n").prependIndent("  ") + "\nRuntime error: $message")
  }
}
