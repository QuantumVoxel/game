package dev.ultreon.quantum.client.gui.widget

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.logger

private val Widget?.path: String
  get() {
    return if (this == null) {
      "unknown"
    } else {
      if (this.parent == null)
        "($id)"
      else if (this.parent == this)
        throw AssertionError("Circular parent reference")
      else "${this.parent.path}.${this.id}"
    }
  }

val Widget.screen: Screen
  get() {
    var current = this
    while (current.parent != null) {
      current = current.parent as Widget
    }
    return current as? Screen ?: throw AssertionError("Disconnected Widget")
  }

abstract class Widget(val parent: GuiContainer?) {
  var positioning: WidgetPositioning = WidgetPositioning.absolute(0f, 0f)
  var id: String = ""
  var x: Float = 0f
  var y: Float = 0f
  var width: Float = 0f
  var height: Float = 0f

  var click: ((count: Int) -> Unit)? = null

  constructor(parent: GuiContainer?, json: JsonValue) : this(parent) {
    id = json["id"]?.asString() ?: throw IllegalArgumentException("Widget must have an id")
    if (!id.matches(Regex("^[_a-zA-Z][a-zA-Z0-9_\\-][a-zA-Z0-9_]*$"))) {
      logger.error("Widget id must be a valid identifier: $id")
      return
    }
    val positionValue = json["position"]
    if (positionValue != null && positionValue.isObject) {
      val type = positionValue["type"]?.asString() ?: run {
        logger.warn("No positioning set for widget $id in ${parent.path}")
        return
      }
      when (type) {
        "absolute" -> {
          x = positionValue["x"].asFloat()
          y = positionValue["y"].asFloat()
        }

        "relative" -> {
          val relativeToValue = positionValue["relative-to"]
          if (relativeToValue != null && relativeToValue.isObject) {
            when (val relativeToType = relativeToValue["type"].asString()) {
              "widget" -> {
                val relativeToId = relativeToValue["id"].asString()
                this.positioning = WidgetPositioning.relativeToWidget(screen.widgets[relativeToId] ?: run {
                  logger.error("Widget not found: $relativeToId")
                  return
                }, positionValue["x"]?.asFloat() ?: 0f, positionValue["y"]?.asFloat() ?: 0f)
              }

              "parent" ->
                this.positioning =
                  WidgetPositioning.relativeToParent(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              "screen" ->
                this.positioning =
                  WidgetPositioning.relativeToScreen(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              "screen-top" ->
                this.positioning =
                  WidgetPositioning.relativeToScreenTop(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              "screen-bottom" ->
                this.positioning =
                  WidgetPositioning.relativeToScreenBottom(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              "screen-left" ->
                this.positioning =
                  WidgetPositioning.relativeToScreenLeft(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              "screen-right" ->
                this.positioning =
                  WidgetPositioning.relativeToScreenRight(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              "screen-center" ->
                this.positioning =
                  WidgetPositioning.relativeToScreenCenter(
                    positionValue["x"]?.asFloat() ?: 0f,
                    positionValue["y"]?.asFloat() ?: 0f
                  )

              else -> {
                logger.error("Invalid relative-to type: $relativeToType")
              }
            }
          } else {
            logger.error("Invalid relative-to value: $relativeToValue")
          }
        }

        else -> {
          logger.error("Invalid position type: $type")
        }
      }
    }

    width = json["width"]?.asFloat() ?: 0f
    height = json["height"]?.asFloat() ?: 0f

    val interactionValue = json["interaction"]
    if (interactionValue != null) {
      if (interactionValue.isObject) {
        val type = interactionValue["on-click"]
        if (type != null && type.isObject) {
          val clickCount = type["count"]?.asInt() ?: 1
          if (clickCount > 0) {
            val actionValue = type["action"]
            if (actionValue != null && actionValue.isObject) {
              val action = UiAction.of(actionValue)
              this.click = { count ->
                if (count == clickCount) {
                  action(this)
                }
              }
            } else {
              logger.error("Invalid action value: $actionValue for widget $path")
            }
          } else {
            logger.warn("Invalid click count: $clickCount for widget $path")
          }
        } else {
          logger.error("Invalid on-click value: $type for widget $path")
        }
      } else {
        logger.error("Invalid interaction value: $interactionValue for widget $path")
      }
    } else {
      logger.warn("No interaction value for widget $path")
    }
  }

  private var pressedPtr: Int = -1
  protected var pressed: Boolean = false
    private set

  private var clicks: Int = 0
  private var lastClick: Int = 0

  abstract fun render(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float)

  fun contains(mouseX: Float, mouseY: Float): Boolean {
    return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
  }

  open fun touchDown(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    pressedPtr = pointer
    pressed = true
    return true
  }

  open fun touchUp(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    if (pressed) {
      pressed = false
      pressedPtr = pointer

      if (lastClick + 1000 > System.currentTimeMillis()) clicks = 0
      clicks++
      click(clicks)
      return true
    }

    return false
  }

  open fun filesDropped(files: List<FileHandle>) {
    // To be implemented
  }

  open fun click(count: Int = 1) {
    click?.invoke(count)
  }

  open fun tick() {
    // To be implemented
  }

  open fun dispose() {
    // To be implemented
  }

  open fun mouseScroll(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
    return false
  }

  open fun preRender(screen: Screen) {
    val positioning = this.positioning.getPosition(
      screen,
      parent?.x ?: 0f,
      parent?.y ?: 0f,
      parent?.width ?: Gdx.graphics.width.toFloat(),
      parent?.height ?: Gdx.graphics.height.toFloat()
    )

    this.x = positioning.first
    this.y = positioning.second
  }
}
