package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi

@ExperimentalApi
interface Networker {

  fun init()
  fun close()
}
