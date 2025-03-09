package dev.ultreon.quantum.client.gui.widget

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.utils.Align
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.widget.button.ButtonType
import dev.ultreon.quantum.client.gui.widget.button.TextButton
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.util.id
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import kotlin.math.max

private val inputEvent = InputEvent()

open class ModSidebar(parent: GuiContainer?) : GuiContainer(parent) {
  private var mod: ModContainer? = null
  protected open var xGoal: Float = -200f

  override var x: Float = -200f
  override var y: Float = 0f

  val contentHeight: Float
    get() = height

  private var scrollY: Float = 0f
    set(value) {
      field = value.coerceIn(0f, max(height - contentHeight, 0f))
    }

  private val descriptionLabel = CompatTypingLabel(this)

  private var widthGoal = 200f

  init {
    positioning = null
    width = 200f
    height = parent?.height ?: 0f
    x = -width

    descriptionLabel.font = quantum.font
    descriptionLabel.setSize(300f, parent?.height ?: 0f)
    descriptionLabel.setWrap(true)
    descriptionLabel.trackingInput = true

    for ((index, mod) in FabricLoader.getInstance().allMods.withIndex()) {
      val modId = mod.metadata.id
      val modName = mod.metadata.name

      add<TextButton> {
        id = "mod-button-$modId"
        type = ButtonType.Inset
        text = modName
        x = 10f
        y = (this@ModSidebar.parent?.height ?: 0f) - (index + 1) * 30f
        width = 180f
        height = 20f
        positioning = null
        click = {
          logger.info("Viewing mod: $modId")
          this@ModSidebar.openView(mod)
        }
      }
    }
  }

  fun openView(mod: ModContainer?) {
    if (mod == null) {
      widthGoal = 200f
      return
    }
    widthGoal = 500f
    this.mod = mod

    descriptionLabel.restart(
      """
[%200][bold][#40ff80]${mod.metadata?.name ?: "Unknown Mod"}
[%100][gold]Version:[lightest grey] ${mod.metadata?.version ?: "0"}
[gold]Contact:[lightest grey] ${
        mod.metadata?.contact?.asMap()?.map { (key, value) ->
          if (value == "Optional.empty") return@map null
          when (key) {
            "website" -> "{TRIGGER=URL|$value}[lighter blue]Website{ENDTRIGGER}[lightest grey]"
            "homepage" -> "{TRIGGER=URL|$value}[lighter blue]Homepage{ENDTRIGGER}[lightest grey]"
            "github" -> "{TRIGGER=URL|$value}[lighter blue]GitHub{ENDTRIGGER}[lightest grey]"
            "gitlab" -> "{TRIGGER=URL|$value}[lighter blue]GitLab{ENDTRIGGER}[lightest grey]"
            "sources" -> "{TRIGGER=URL|$value}[lighter blue]Sources{ENDTRIGGER}[lightest grey]"

            "bitbucket" -> "{TRIGGER=URL|$value}[lighter blue]BitBucket{ENDTRIGGER}[lightest grey]"
            "youtube" -> "{TRIGGER=URL|$value}[lighter blue]YouTube{ENDTRIGGER}[lightest grey]"
            "twitch" -> "{TRIGGER=URL|$value}[lighter blue]Twitch{ENDTRIGGER}[lightest grey]"
            "twitter" -> "{TRIGGER=URL|$value}[lighter blue]Twitter{ENDTRIGGER}[lightest grey]"
            "discord" -> "{TRIGGER=URL|$value}[lighter blue]Discord{ENDTRIGGER}[lightest grey]"
            "reddit" -> "{TRIGGER=URL|$value}[lighter blue]Reddit{ENDTRIGGER}[lightest grey]"
            "curseforge" -> "{TRIGGER=URL|$value}[lighter blue]CurseForge{ENDTRIGGER}[lightest grey]"
            "email" -> "{TRIGGER=URL|mailto:$value}[lighter blue]Email{ENDTRIGGER}[lightest grey]"
            "issues" -> "{TRIGGER=URL|$value}[lighter blue]Issues{ENDTRIGGER}[lightest grey]"
            "irc" -> "{TRIGGER=URL|$value}[lighter blue]IRC{ENDTRIGGER}[lightest grey]"
            else -> "($key)"
          }
        }?.joinToString(", ") ?: "Unknown"
      }
[gold]Authors:[lightest grey] ${
        mod.metadata?.authors?.joinToString(", ") {
          if (it.contact["homepage"]?.isPresent == true) "{TRIGGER=URL|${it.contact["homepage"].get()}}[lighter blue]${it.name}[grey]{ENDTRIGGER}"
          else it.name
        } ?: "Unknown"
      }
[gold]License:[lightest grey] ${mod.metadata?.license ?: "All-Rights-Reserved"}
[gold]Description:[lightest grey]
${mod.metadata?.description ?: "..."}

[gold]Credits:[lightest grey]
${mod.metadata?.contributors?.joinToString(", ") {
        if (it.contact["homepage"]?.isPresent == true) "{TRIGGER=URL|${it.contact["homepage"].get()}}[lighter blue]${it.name}[grey]{ENDTRIGGER}"
        else it.name
      } ?: "Unknown"}
""".trim())
    descriptionLabel.alignment = Align.topLeft
    descriptionLabel.skipToTheEnd()
    descriptionLabel.cancelSkipping()
  }

  override fun renderBackground(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    height = parent?.height ?: 0f

    if (width > 200f) {
      renderer.drawNinePatch(
        id(path = "textures/gui/frames/dark.png"),
        7F,
        x + 193f,
        height + 7F,
        width - 200F + 7f,
        (parent?.height ?: 0f) + 14F,
        21,
        21
      )
      val w = max(300F, width - 200F) - 3
      renderer.subInstance(x + width - 300F, 0F, w, height) {
        descriptionLabel.width = w
        it.drawText(descriptionLabel, 10f, -10f)
      }
    }
    renderer.drawNinePatch(
      id(path = "textures/gui/frames/dark.png"),
      7F,
      -7F,
      height + 7F,
      207F,
      (parent?.height ?: 0f) + 14F,
      21,
      21
    )
  }

  @Suppress("GDXKotlinFlushInsideLoop")
  override fun renderChildren(renderer: GuiRenderer, mouseX: Int, mouseY: Int, delta: Float) {
    for (child in children) {
      renderer.subInstance(child.x, child.y - scrollY, child.width, child.height) {
        renderChild(it, child, mouseX, (mouseY + scrollY).toInt(), delta)
      }
    }
  }

  override fun touchDown(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    super.touchDown(x, y, button, pointer)
    if (x > 200f) {
      descriptionLabel.fire(inputEvent.also {
        it.pointer = pointer
        it.button = button
        it.stageX = x - 200f
        it.stageY = y - scrollY
        it.type = InputEvent.Type.touchDown
      })
    }
    return true
  }

  override fun touchUp(x: Float, y: Float, button: Int, pointer: Int): Boolean {
    if (!super.touchUp(x, y, button, pointer)) {
      if (this.x == 0f && width == 500f) {
        if (x < 200f) {
          openView(null)
        }
      }
    }
    if (x > 200f) {
      descriptionLabel.fire(inputEvent.also {
        it.pointer = pointer
        it.button = button
        it.stageX = x - 200f
        it.stageY = y - scrollY
        it.type = InputEvent.Type.touchUp
      })
    }
    return true
  }

  override fun mouseScroll(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
    if (super.mouseScroll(x, y, deltaX, deltaY)) return true

    scrollY += deltaY
    return true
  }

  override fun tick() {
    if ((Gdx.input.x / quantum.guiScale < 40 || Gdx.input.x / quantum.guiScale < x + width) && !((Gdx.input.x == 0 && Gdx.input.y == 0) || (Gdx.input.x < 0 || Gdx.input.y < 0 || Gdx.input.x > Gdx.graphics.width || Gdx.input.y > Gdx.graphics.height))) {
      this.open()
    } else {
      this.close()
    }

    if (width != widthGoal) {
      val fl = width + (widthGoal - width) / 5f
      width = fl

      if (width in ((widthGoal - 1f)..(widthGoal + 1f))) {
        width = widthGoal
      }
    }
    if (x != xGoal) {
      val fl = x + (xGoal - x) / 5f
      x = fl

      if (x in ((xGoal - 1f)..(xGoal + 1f))) {
        x = xGoal
      }
    }
  }

  private fun open() {
    if (xGoal == 0f) return
    xGoal = 0f

    logger.debug("Opening sidebar: $xGoal")
  }

  private fun close() {
    if (xGoal == -200f) return
    widthGoal = 200f
    xGoal = -200f

    logger.debug("Closing sidebar: $xGoal")
  }
}
