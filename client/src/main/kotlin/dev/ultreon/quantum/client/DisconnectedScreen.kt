package dev.ultreon.quantum.client

import dev.ultreon.quantum.client.gui.screens.*
import dev.ultreon.quantum.json
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.util.id
import ktx.json.fromJson

class DisconnectedScreen(val message: String) : Screen() {
  override fun setup() {
    add(Text(this, json.fromJson("""
      {
        "type": "text",
        "id": "title",
        "text": "${message.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t").replace("\"", "\\\"")}",
        "appearance": {
          "color": "#ffffff"
        },
        "position": {
          "type": "relative",
          "relative-to": {
            "type": "screen-center"
          },
          "y": 100
        }
      }
    """.trimIndent())))

    add(TextButton(this, json.fromJson("""
      {
        "type": "button",
        "id": "exit-world",
        "text": "Go back to title",
        "size": [150, 21],
        "position": {
          "type": "relative",
          "relative-to": {
            "type": "screen-center"
          },
          "y": 100
          "x": -75
        }
      }
    """.trimIndent())).also {
      click = {
        quantum.showScreen(IdScreen.get(id(path = "title")) ?: run {
          logger.error("Screen not found: title")
          return@run PlaceholderScreen
        })
      }
    })

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
