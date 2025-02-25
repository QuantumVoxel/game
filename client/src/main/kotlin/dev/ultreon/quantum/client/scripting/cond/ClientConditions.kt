package dev.ultreon.quantum.client.scripting.cond

import com.badlogic.gdx.utils.Os
import com.badlogic.gdx.utils.SharedLibraryLoader
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.scripting.condition.VirtualCondition

object ClientConditions {
  val platform = VirtualCondition { callContext ->
    val value = callContext.getString("platform") ?: return@VirtualCondition false
    when (value) {
      "client" -> true
      "desktop" -> SharedLibraryLoader.os in setOf(Os.Windows, Os.MacOsX, Os.Linux)
      "linux" -> SharedLibraryLoader.os == Os.Linux
      "macos" -> SharedLibraryLoader.os == Os.MacOsX
      "windows" -> SharedLibraryLoader.os == Os.Windows
      "neonix-os" -> System.getProperty("os.name").equals("NeonixOS")
      "android" -> SharedLibraryLoader.os == Os.Android
      "ios" -> SharedLibraryLoader.os == Os.IOS
      "web" -> SharedLibraryLoader.os == null && (gamePlatform.isWebGL3 || gamePlatform.isWebGL2)
      "switch" -> gamePlatform.isSwitchGDX
      "xbox" -> false // TODO
      "ps4" -> false // TODO
      "ps5" -> false // TODO
      "wiiu" -> false // TODO
      "wii" -> false // TODO
      "n3ds" -> false // TODO
      "n64" -> false // TODO
      "gba" -> false // TODO
      "gamecube" -> false // TODO
      "nds" -> false // TODO
      "mobile" -> gamePlatform.isMobile
      else -> false
    }
  }.register("platform")
}
