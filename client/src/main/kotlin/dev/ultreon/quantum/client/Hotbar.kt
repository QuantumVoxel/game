package dev.ultreon.quantum.client

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.client.gui.widget.Widget
import dev.ultreon.quantum.util.id

private val hotbarLeft = id(path = "textures/gui/hud/hotbar/hotbar_left.png")
private val hotbarRight = id(path = "textures/gui/hud/hotbar/hotbar_right.png")
private val hotbarDisplay = id(path = "textures/gui/hud/hotbar/hotbar_display.png")
private val hotbarBridge = id(path = "textures/gui/hud/hotbar/hotbar_bridge.png")
private val hotbarSelect = id(path = "textures/gui/hud/hotbar/hotbar_select.png")

/**
 * The hotbar widget.
 * This widget is used to display the hotbar on the screen.
 *
 * @param parent The parent container of this widget.
 * @constructor Creates a new Hotbar.
 */
class Hotbar : Widget {
  /**
   * The index of the hotbar.
   */
  val index: Int
    get() {
      val player = quantum.player
      val inventory = player?.inventoryComponent
      return inventory?.hotbarIndex ?: 0
    }

  /**
   * Creates a new Hotbar.
   *
   * @param parent The parent container of this widget.
   */
  constructor(parent: GuiContainer) : super(parent)

  /**
   * Creates a new Hotbar.
   *
   * @param parent The parent container of this widget.
   * @param json The json value of this widget.
   */
  constructor(parent: GuiContainer, json: JsonValue) : super(parent, json)

  /**
   * This function is called every frame and is responsible for rendering the widget.
   *
   * @param renderer The renderer to render the widget with.
   * @param mouseX The x position of the mouse.
   * @param mouseY The y position of the mouse.
   * @param delta The time in seconds since the last frame.
   */
  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    if (width > 208) renderer.drawTexture(hotbarBridge, x + 100, y, width - 200, 32F)
    renderer.drawTexture(hotbarLeft, x, y, 104F, 32F)
    renderer.drawTexture(hotbarRight, x + width - 104, y, 104F, 32F)

    renderer.drawTexture(hotbarDisplay, x + 1F, y + 32F, 102F, 16F)
    renderer.drawTexture(hotbarDisplay, x + width - 103F, y + 32F, 102F, 16F)

    val value: Int = if (index >= 5) (width - 104 + 5 - 105 + 5).toInt() else 0
    renderer.drawTexture(hotbarSelect, x + 5 + index * 19 + value, y + 8, 18F, 18F)
  }
}
