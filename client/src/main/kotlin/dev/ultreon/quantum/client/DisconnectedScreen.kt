package dev.ultreon.quantum.client

import dev.ultreon.quantum.client.gui.screens.*
import dev.ultreon.quantum.client.gui.widget.Text
import dev.ultreon.quantum.client.gui.widget.button.TextButton
import dev.ultreon.quantum.client.gui.widget.WidgetPositioning

class DisconnectedScreen(val message: String) : Screen() {
  override fun setup() {
//    add(Text(this, json.fromJson("""
//      {
//        "type": "text",
//        "id": "title",
//        "text": "${message.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t").replace("\"", "\\\"")}",
//        "appearance": {
//          "color": "#ffffff"
//        },
//        "position": {
//          "type": "relative",
//          "relative-to": {
//            "type": "screen-center"
//          },
//          "y": 100
//        }
//      }
//    """.trimIndent())))

    add<Text> {
      id = "title"
      text = "[#ffffff]$message"
      positioning = WidgetPositioning.relativeToScreenCenter(relativeY = 100f)
    }

    add<TextButton> {
      id = "exit-world"
      text = "Go back to title"
      width = 150f
      height = 21f
      positioning = WidgetPositioning.relativeToScreenCenter(relativeX = -75f, relativeY = 100f)

      click = {
        quantum.showScreen(TitleScreen())
      }
    }

//    add(TextButton(this, json.fromJson("""
//      {
//        "type": "button",
//        "id": "reconnect",
//        "text": "Reconnect",
//        "size": [150, 21],
//        "position": {
//          "type": "relative",
//          "relative-to": {
//            "type": "screen-center"
//          },
//          "y": 100
//          "x": 75
//        }
//      }
//    """.trimIndent())).also {
//      click = {
//        quantum.showScreen(PlaceholderScreen)
//      }
//    })
  }
}
