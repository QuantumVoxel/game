package dev.ultreon.quantum.client.gui.widget.button

import com.badlogic.gdx.utils.GdxRuntimeException
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.util.id

private val darkNormal = id(path = "textures/gui/buttons/dark.png")
private val darkHover = id(path = "textures/gui/buttons/dark_hover.png")
private val darkHoverPressed = id(path = "textures/gui/buttons/dark_pressed_hover.png")
private val darkPressed = id(path = "textures/gui/buttons/dark_pressed.png")
private val darkDisabled = id(path = "textures/gui/buttons/dark_disabled.png")
private val darkDisabledHover = id(path = "textures/gui/buttons/dark_disabled_hover.png")
private val darkDisabledPressed = id(path = "textures/gui/buttons/dark_disabled_pressed.png")

private val darkInset = id(path = "textures/gui/buttons/dark_inset.png")
private val darkHoverInset = id(path = "textures/gui/buttons/dark_inset_hover.png")
private val darkHoverPressedInset = id(path = "textures/gui/buttons/dark_inset_pressed_hover.png")
private val darkPressedInset = id(path = "textures/gui/buttons/dark_inset_pressed.png")
private val darkDisabledInset = id(path = "textures/gui/buttons/dark_inset_disabled.png")
private val darkDisabledHoverInset = id(path = "textures/gui/buttons/dark_inset_disabled_hover.png")
private val darkDisabledPressedInset = id(path = "textures/gui/buttons/dark_inset_disabled_pressed.png")

enum class ButtonType {
  Normal {
    override fun texture(pressed: Boolean, hover: Boolean, disabled: Boolean): NamespaceID {
      return when {
        disabled -> when {
          hover -> darkDisabledHover
          pressed -> darkDisabledPressed
          else -> darkDisabled
        }
        hover -> when {
          pressed -> darkHoverPressed
          else -> darkHover
        }
        pressed -> darkPressed
        else -> darkNormal
      }
    }
  },
  Inset {
    override fun texture(pressed: Boolean, hover: Boolean, disabled: Boolean): NamespaceID {
      return when {
        disabled -> when {
          hover -> darkDisabledHoverInset
          pressed -> darkDisabledPressedInset
          else -> darkDisabledInset
        }
        hover -> when {
          pressed -> darkHoverPressedInset
          else -> darkHoverInset
        }
        pressed -> darkPressedInset
        else -> darkInset
      }
    }
  };

  open fun texture(pressed: Boolean, hover: Boolean, disabled: Boolean): NamespaceID {
    throw GdxRuntimeException("Not implemented")
  }
}
