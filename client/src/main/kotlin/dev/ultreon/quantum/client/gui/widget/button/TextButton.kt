package dev.ultreon.quantum.client.gui.widget.button

import com.badlogic.gdx.utils.JsonValue
import com.github.tommyettinger.textra.Layout
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.client.gui.widget.Widget
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger

/**
 * The `TextButton` class represents a button widget in the GUI, which can display text and handle user interactions.
 * It extends the `Widget` class and allows customization of its appearance, including text alignment, color, and size.
 * The button's behavior can be controlled through its enabled state, and it supports different visual states like normal, pressed, and disabled.
 * The `TextButton` can be initialized with JSON configuration, making it flexible for integration into various GUI screens.
 *
 * ### Example Usage:
 * ```kotlin
 * import dev.ultreon.quantum.client.gui.widget.button.TextButton
 * import dev.ultreon.quantum.client.gui.widget.WidgetPositioning
 * import dev.ultreon.quantum.client.gui.screens.Screen
 * import dev.ultreon.quantum.client.gui.screens.PlaceholderScreen
 *
 * class MyScreen : Screen {
 *   override fun setup() {
 *     this.add<TextButton>() {
 *       text = "Start Game"
 *       width = 100f
 *       height = 20f
 *       positioning = WidgetPositioning.relativeToScreenCenter(relativeX = -50f)
 *       onClick { quantum.showScreen(PlaceholderScreen) }
 *     }
 *   }
 * }
 * ```
 */
class TextButton : Widget {
  var type: ButtonType

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
      textLabel.reset()
      quantum.font.markup(value, textLabel)
      field = value
    }

  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    renderer.drawNinePatch(
      texture = type.texture(pressed, isMouseOver(mouseX, mouseY), !enabled),
      inset = 7F,
      x = 0F,
      y = height,
      width = width,
      height = height,
      texWidth = 21,
      texHeight = 21
    )

    renderer.drawText(layout = textLabel, width * textAlignment - textLabel.width * textAlignment, height-height / 2 + 4 - (if (pressed) 2 else 0))
  }

  private fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
    return mouseX >= 0 && mouseX < width && mouseY >= 0 && mouseY < height
  }
}
