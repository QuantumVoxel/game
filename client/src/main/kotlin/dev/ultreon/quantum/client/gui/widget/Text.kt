package dev.ultreon.quantum.client.gui.widget

import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger

class Text : Widget {
  constructor(parent: GuiContainer?, widget: JsonValue) : super(parent, widget) {
    val text = widget["text"]?.asString() ?: "..."
    var color = widget["appearance"]?.run { this["color"]?.asString() ?: "#ffffff" } ?: "#ffffff"
    if (!color.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"))) {
      logger.error("Invalid color: $color")
      color = "#ffffff"
    }

    this.text = "[$color]$text"

    clipping = false
    align = Align.center
  }

  val textLabel = CompatTypingLabel(this).also {
    it.font = quantum.font
    it.width = 0F
    it.height = 0F
  }

  constructor(parent: GuiContainer?) : super(parent) {
    text = "..."
    clipping = false
    align = Align.center
  }

  var text = "..."
    set(value) {
      textLabel.restart(value)
      textLabel.skipToTheEnd()
      textLabel.cancelSkipping()
      textLabel.layout()

      field = value
    }

  var align: Int = Align.center
    set(value) {
      textLabel.alignment = value
      textLabel.setOrigin(align)
      textLabel.layout()

      field = value
    }

  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    textLabel.setPosition(0f, 0f)
    textLabel.setSize(0f, 0f)
    renderer.drawText(textLabel)
  }
}
