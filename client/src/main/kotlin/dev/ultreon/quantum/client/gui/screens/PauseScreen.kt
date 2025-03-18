package dev.ultreon.quantum.client.gui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Align
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.client.TitleScreen
import dev.ultreon.quantum.client.gui.widget.ModSidebar
import dev.ultreon.quantum.client.gui.widget.Text
import dev.ultreon.quantum.client.gui.widget.WidgetPositioning
import dev.ultreon.quantum.client.gui.widget.button.TextButton
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin

class PauseScreen : Screen() {
  init {
    title = "Quantum Voxel"
  }

  override fun setup() {
    add<Text> {
      id = "title"
      text = "[%200][lighter red]Game Menu"
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
      id = "resume"
      text = "Resume Game"
      width = 100f
      height = 30f
      positioning = WidgetPositioning.relativeToScreenCenter(relativeX = -50f)
      click = {
        QuantumVoxel.instance.showScreen(PlaceholderScreen)
        Gdx.input.isCursorCatched = true
      }
    }

    add<TextButton> {
      id = "main-menu"
      text = "Main Menu"
      width = 100f
      height = 30f
      positioning = WidgetPositioning.relativeToScreenCenter(relativeX = -50f, relativeY = -50f)
      click = {
        QuantumVoxel.instance.stopWorld {
          QuantumVoxel.instance.showScreen(TitleScreen())
        }
      }
    }

    add<ModSidebar> { id = "mod-sidebar" }
  }
}
