package dev.ultreon.quantum.scripting.scripts

import dev.ultreon.scriptic.ScripticLang
import dev.ultreon.scriptic.lang.obj.Event

/**
 * Contains all the events used in Quantum.
 *
 * @property whenCalledEvt The event that is triggered when a function is called.
 */
object QuantumEvents {
  val whenCalledEvt = ScripticLang.registerEvent("^when called:$", Event())
}
