package dev.ultreon.quantum.client.gui.widget

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.github.tommyettinger.textra.TypingAdapter
import com.github.tommyettinger.textra.TypingLabel
import com.github.tommyettinger.textra.TypingListener
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger

private val event: InputEvent = InputEvent()

class CompatTypingLabel(parent: Widget) : TypingLabel(), TypingListener {
  private val qParent: Widget = parent
  private val temp = Vector2()

  var urlClickListener: (String) -> Unit = {
    val screen = quantum.screen
    if (screen is Screen) {
      quantum.showScreen(ConfirmationScreen(screen, "[lighter red]Do you want to open this link?", it) {
        Gdx.net.openURI(it)
      })
    }
  }

  init {
    typingListener = object : TypingAdapter() {
      override fun event(event: String?) {
        super.event(event)

        if (event != null) {
          val split = event.split("|", limit = 2)
          if (split.size == 2) {
            val type = split[0].lowercase()
            val value = split[1]
            when (type) {
              "url" -> urlClickListener?.invoke(value)
//              "command" -> quantum.commandHandler.handleCommand(value)
//              "suggest" -> quantum.commandHandler.suggestCommand(value)
              "copy" -> quantum.clipboard.contents = value
              else -> logger.warn("Unknown event type: $type")
            }
          }
        }
      }
    }
  }

  override fun screenToLocalCoordinates(screenCoords: Vector2): Vector2 {
    val sub = screenCoords.set(screenCoords.x / quantum.guiScale, screenCoords.y / quantum.guiScale)
      .sub(quantum.guiRenderer.translation(temp))
    return sub
  }

  fun touchUp(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    return fire(event.also {
      it.pointer = pointer
      it.button = button
      it.stageX = x
      it.stageY = y
      it.type = InputEvent.Type.touchUp
    })
  }

  fun touchDown(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    return fire(event.also {
      it.pointer = pointer
      it.button = button
      it.stageX = x
      it.stageY = y
      it.type = InputEvent.Type.touchDown
    })
  }

  override fun event(event: String?) {

  }

  override fun end() {

  }

  override fun replaceVariable(variable: String?): String? {
    return null
  }

  override fun onChar(ch: Long) {

  }
}
