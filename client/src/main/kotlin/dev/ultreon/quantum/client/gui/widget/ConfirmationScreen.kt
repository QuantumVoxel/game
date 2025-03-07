package dev.ultreon.quantum.client.gui.widget

import com.badlogic.gdx.utils.Align
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.client.gui.widget.button.TextButton
import dev.ultreon.quantum.client.quantum

class ConfirmationScreen(val screen: Screen, title: String, val message: String, val approved: () -> Boolean) : Screen(screen) {
  init {
    this.title = title
  }

  override fun setup() {
    add<Text> {
      id = "title"
      text = "[%200]$title\n[ ][%100]$message"
      positioning = WidgetPositioning.relativeToScreenCenter(relativeY = 100f)

      align = Align.center
    }

    add<TextButton> {
      id = "yes"
      text = "Yes"
      width = 95f
      height = 30f
      positioning = WidgetPositioning.relativeToScreenCenter(relativeX = -100f)
      click = {
        approved()
        quantum.showScreen(screen)
      }
    }

    add<TextButton> {
      id = "no"
      text = "No"
      width = 95f
      height = 30f
      positioning = WidgetPositioning.relativeToScreenCenter(relativeX = 5f)
      click = {
        denied()
      }
    }
  }

  fun denied() = quantum.showScreen(screen)
}
