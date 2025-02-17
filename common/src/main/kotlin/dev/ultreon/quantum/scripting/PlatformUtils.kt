package dev.ultreon.quantum.scripting

import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.Os
import com.badlogic.gdx.utils.SharedLibraryLoader
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.scripting.function.function

object PlatformUtils : ContextAware<PlatformUtils> {
  override val persistentData = PersistentData()
  override fun contextType(): ContextType<PlatformUtils> {
    return ContextType.platform
  }

  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "os" -> ContextValue(ContextType.string, System.getProperty("os.name"))
      "arch" -> ContextValue(ContextType.string, System.getProperty("os.arch"))
      "java_version" -> ContextValue(ContextType.string, System.getProperty("java.version"))
      "java_vendor" -> ContextValue(ContextType.string, System.getProperty("java.vendor"))
      "java_home" -> ContextValue(ContextType.string, System.getProperty("java.home"))
      "java_specification_version" -> ContextValue(ContextType.int, System.getProperty("java.specification.version").toInt())
      "java_specification_vendor" -> ContextValue(ContextType.string, System.getProperty("java.specification.vendor"))

      "is_windows" -> ContextValue(ContextType.boolean, gamePlatform.isWindows)
      "is_linux" -> ContextValue(ContextType.boolean, gamePlatform.isLinux)
      "is_macos" -> ContextValue(ContextType.boolean, gamePlatform.isMac)
      "is_android" -> ContextValue(ContextType.boolean, gamePlatform.isAndroid)
      "is_ios" -> ContextValue(ContextType.boolean, gamePlatform.isIos)
      "is_web" -> ContextValue(ContextType.boolean, gamePlatform.isWebGL3 || gamePlatform.isWebGL2)
      "is_switch" -> ContextValue(ContextType.boolean, gamePlatform.isSwitch)
      "is_uwp" -> ContextValue(ContextType.boolean, gamePlatform.isUWP)

      "is_mobile" -> ContextValue(ContextType.boolean, gamePlatform.isMobile)
      "is_desktop" -> ContextValue(ContextType.boolean, gamePlatform.isDesktop)
      "is_switchgdx" -> ContextValue(ContextType.boolean, gamePlatform.isSwitchGDX)

      "is_neonix_os" -> ContextValue(ContextType.boolean, System.getProperty("os.name").equals("NeonixOS"))

      "is_debug" -> ContextValue(ContextType.boolean, gamePlatform.isDebug)

      "is_server" -> ContextValue(ContextType.boolean, gamePlatform.isServer)
      "is_client" -> ContextValue(ContextType.boolean, gamePlatform.isClient)

      "is_gles3" -> ContextValue(ContextType.boolean, gamePlatform.isGLES3)
      "is_gles2" -> ContextValue(ContextType.boolean, gamePlatform.isGLES2)
      "is_gl46" -> ContextValue(ContextType.boolean, gamePlatform.isGL46)
      "is_gl45" -> ContextValue(ContextType.boolean, gamePlatform.isGL45)
      "is_gl44" -> ContextValue(ContextType.boolean, gamePlatform.isGL44)
      "is_gl43" -> ContextValue(ContextType.boolean, gamePlatform.isGL43)
      "is_gl42" -> ContextValue(ContextType.boolean, gamePlatform.isGL42)
      "is_gl41" -> ContextValue(ContextType.boolean, gamePlatform.isGL41)
      "is_gl40" -> ContextValue(ContextType.boolean, gamePlatform.isGL40)
      "is_gl33" -> ContextValue(ContextType.boolean, gamePlatform.isGL33)
      "is_gl32" -> ContextValue(ContextType.boolean, gamePlatform.isGL32)
      "is_gl31" -> ContextValue(ContextType.boolean, gamePlatform.isGL31)
      "is_gl30" -> ContextValue(ContextType.boolean, gamePlatform.isGL30)
      "is_gl20" -> ContextValue(ContextType.boolean, gamePlatform.isGL20)
      "is_webgl3" -> ContextValue(ContextType.boolean, gamePlatform.isWebGL3)
      "is_webgl2" -> ContextValue(ContextType.boolean, gamePlatform.isWebGL2)
      else -> null
    }
  }
}
