package dev.ultreon.quantum.client.gui.widget

import dev.ultreon.quantum.InternalApi
import dev.ultreon.quantum.client.WidgetFactories
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.logger

open class GuiContainer(parent: GuiContainer?) : Widget(parent) {
  private val children = mutableListOf<Widget>()
  val widgets: MutableMap<String, Widget> = mutableMapOf()

  @OptIn(InternalApi::class)
  inline fun <reified T : Widget> add(block: T.() -> Unit): T {
    return add(WidgetFactories.create<T>(this).apply(block))
  }

  @InternalApi
  fun <T : Widget> add(widget: T): T {
    if (widget.id in widgets) {
      logger.warn("Duplicate widget id in container: ${widget.id}")
      val remove = widgets.remove(widget.id)
      remove?.dispose()
      screen.widgets.remove(widget.id)
    }
    widgets[widget.id] = widget
    if (widget.id in screen.widgets) {
      logger.warn("Duplicate widget id in screen: ${widget.id}")
      val remove = screen.widgets.remove(widget.id)
      remove?.dispose()
    }
    screen.widgets[widget.id] = widget
    children += widget
    return widget
  }

  override fun preRender(screen: Screen) {
    super.preRender(screen)

    for (child in children) {
      child.preRender(screen)
    }
  }

  override fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    this.renderBackground(renderer, mouseX, mouseY, delta)
    this.renderChildren(renderer, mouseX, mouseY, delta)
  }

  open fun renderBackground(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {

  }

  open fun renderChildren(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    for (child in children) {
      renderChild(renderer, child, mouseX, mouseY, delta)
    }
  }

  open fun renderChild(renderer: GuiRenderer, child: Widget, mouseX: Int, mouseY: Int, delta: Float) {
    child.render(renderer, (mouseX - child.x).toInt(), (mouseY - child.y).toInt(), delta)
  }

  override fun touchDown(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    for (child in children) {
      if (child.contains(x, y) && child.touchDown(x - child.x, y - child.y, button, pointer)) {
        return true
      }
    }

    return false
  }

  override fun touchUp(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    for (child in children) {
      if (child.touchUp(x - child.x, y - child.y, button, pointer)) {
        return true
      }
    }

    return false
  }

  override fun mouseScroll(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
    for (child in children) {
      if (child.mouseScroll(x, y, deltaX, deltaY)) {
        return true
      }
    }

    return false
  }

  override fun dispose() {
    for (child in children) {
      child.dispose()
    }
  }

  override fun tick() {
    for (child in children) {
      child.tick()
    }
  }
}
