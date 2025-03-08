@file:JvmName("Lwjgl3Launcher")

package dev.ultreon.quantum.lwjgl3

import net.fabricmc.loader.impl.launch.knot.KnotClient

fun main(args: Array<String>) {
  System.setProperty("fabric.development", "false")
  System.setProperty("fabric.skipMcProvider", "true")
  KnotClient.main(args)
}
