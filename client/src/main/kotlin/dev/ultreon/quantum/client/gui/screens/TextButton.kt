package dev.ultreon.quantum.client.gui.screens

import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.JsonValue
import com.github.tommyettinger.textra.Layout
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.client.gui.widget.Widget
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger
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
private val darkHoverInset = id(path = "textures/gui/buttons/dark_hover_inset.png")
private val darkHoverPressedInset = id(path = "textures/gui/buttons/dark_pressed_hover_inset.png")
private val darkPressedInset = id(path = "textures/gui/buttons/dark_pressed_inset.png")
private val darkDisabledInset = id(path = "textures/gui/buttons/dark_disabled_inset.png")
private val darkDisabledHoverInset = id(path = "textures/gui/buttons/dark_disabled_hover_inset.png")
private val darkDisabledPressedInset = id(path = "textures/gui/buttons/dark_disabled_pressed_inset.png")

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

class TextButton : Widget {
  val type: ButtonType

  constructor(parent: GuiContainer?, widget: JsonValue, type: ButtonType = ButtonType.Normal) : super(parent, widget) {
    this.type = type
    this.enabled = widget["enabled"]?.asBoolean() ?: true
    this.textAlignment = when (widget["text-alignment"]?.asString()) {
      "left" -> 0.0F
      "center" -> 0.5F
      "right" -> 1.0F
      else -> 0.5F
    }
    this.textLabel = Layout()
    val text = widget["text"]?.asString() ?: "..."
    var color = widget["appearance"]?.run { this["color"]?.asString() ?: "#ffffff" } ?: "#ffffff"
    var size = widget["size"]?.let {
      if (it.isArray) {
        it.asIntArray()
      } else if (it.isObject) {
        intArrayOf(it["width"]?.asInt() ?: it["x"]?.asInt() ?: 21, it["height"]?.asInt() ?: it["y"]?.asInt() ?: 21)
      } else if (it.isNumber) {
        intArrayOf(it.asInt(), it.asInt())
      } else {
        null
      }
    } ?: intArrayOf(21, 21)
    if (size.isEmpty()) {
      size = intArrayOf(21, 21)
    } else if (size.size == 1) {
      size = intArrayOf(size[0], size[0])
    }
    width = size[0].toFloat()
    height = size[1].toFloat()
    if (!color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
      logger.error("Invalid color: $color")
      color = "#ffffff"
    }
    this.text = "[$color]$text"
  }

  constructor(parent: GuiContainer?, type: ButtonType = ButtonType.Normal) : super(parent) {
    this.type = type
    this.enabled = true
    this.textAlignment = 0.5F
    this.textLabel = Layout()
    this.width = 21F
    this.height = 21F
    quantum.font.markup("...", textLabel)
  }

  private val enabled: Boolean
  private val textAlignment: Float
  private var textLabel: Layout

  var text: String = ""
    set(value) {
      quantum.font.markup(value, textLabel)
      field = value
    }

  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    renderer.drawNinePatch(
      texture = type.texture(pressed, isMouseOver(mouseX, mouseY), !enabled),
      inset = 7F,
      x = x,
      y = y,
      width = width,
      height = height,
      texWidth = 21,
      texHeight = 21
    )

    renderer.drawText(layout = textLabel, x + width * textAlignment - textLabel.width * textAlignment, y - height / 2 + 4 - (if (pressed) 2 else 0))
  }

  private fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
    return mouseX >= 0 && mouseX < width && mouseY >= 0 && mouseY < height
  }
}
