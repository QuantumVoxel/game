package dev.ultreon.quantum.scripting.qfunc

class QFuncSyntaxError(message: String) : RuntimeException(message) {
  var position: Int? = null
    private set

  constructor(message: String, position: Int, line: Int, column: Int) : this("$message at $line:$column") {
    this.position = position
  }
}
