package dev.ultreon.quantum.client

import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.client.gui.screens.Text
import dev.ultreon.quantum.client.gui.screens.TextButton
import dev.ultreon.quantum.client.gui.widget.WidgetPositioning
import net.fabricmc.loader.api.FabricLoader

class TitleScreen : Screen() {
  init {
    title = "Quantum Voxel"
  }

  override fun setup() {
    add<Text> {
      id = "title"
      text = "Title Screen"
      positioning = WidgetPositioning.relativeToScreenCenter(relativeY = 100f)
    }

    add<Text> {
      id = "mod-count"
      text = "Mods: ${FabricLoader.getInstance().allMods.size}"
      positioning = WidgetPositioning.relativeToScreenBottom(relativeY = -30f)
    }

    add<TextButton> {
      id = "play"
      text = "Play"
      width = 100f
      height = 50f
      positioning = WidgetPositioning.relativeToScreenCenter(relativeX = -50f)
      click = {
        QuantumVoxel.instance.startWorld()
      }
    }
  }

}
