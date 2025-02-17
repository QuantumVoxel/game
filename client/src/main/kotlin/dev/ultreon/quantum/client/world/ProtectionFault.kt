package dev.ultreon.quantum.client.world

import dev.ultreon.quantum.gamePlatform

private var thrownBefore = false

class ProtectionFault(message: String) : Error(message) {
  init {
    if (thrownBefore) gamePlatform.halt(13)
    thrownBefore = true
  }
}
