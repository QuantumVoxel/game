package dev.ultreon.quantum.client.gui.screens

import com.badlogic.gdx.utils.JsonValue
import com.github.tommyettinger.textra.Layout
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.client.gui.widget.Widget
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger

class Text : Widget {
  constructor(parent: GuiContainer?, widget: JsonValue) : super(parent, widget) {
    this.textLabel = Layout()
    val text = widget["text"]?.asString() ?: "..."
    var color = widget["appearance"]?.run { this["color"]?.asString() ?: "#ffffff" } ?: "#ffffff"
    if (!color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
      logger.error("Invalid color: $color")
      color = "#ffffff"
    }

    textLabel.reset()
    this.text = "[$color]$text"
  }

  constructor(parent: GuiContainer?) : super(parent) {
    this.textLabel = Layout()
  }

  private var textLabel: Layout

  var textAlignment = 0.5F

  var text = "..."
    set(value) {
      textLabel.reset()
      quantum.font.markup(value, textLabel)
      width = textLabel.width
      height = quantum.bitmapFont.lineHeight

      field = value
    }

  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    renderer.drawText(layout = textLabel, x + width * textAlignment - textLabel.width * textAlignment, y)
  }
}
