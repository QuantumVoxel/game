package dev.ultreon.quantum.client

import com.badlogic.gdx.utils.Align
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.client.gui.widget.ModSidebar
import dev.ultreon.quantum.client.gui.widget.Text
import dev.ultreon.quantum.client.gui.widget.WidgetPositioning
import dev.ultreon.quantum.client.gui.widget.button.TextButton
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin

class TitleScreen : Screen() {
  init {
    title = "Quantum Voxel"
  }

  override fun setup() {
    add<Text> {
      id = "title"
      text = "[%200][lighter red]Title Screen"
      positioning = WidgetPositioning.relativeToScreenCenter(relativeY = 100f)
      align = Align.center
    }

    add<Text> {
      id = "mod-count"
      text = "${FabricLoader.getInstance().allMods.size} mods loaded / ${FabricLoader.getInstance().allMods.filter { it.origin.kind == ModOrigin.Kind.NESTED }.size} embedded mods"
      positioning = WidgetPositioning.absolute(20f, 20f)
      align = Align.bottomLeft
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

    add<ModSidebar> { id = "mod-sidebar" }
  }

}
