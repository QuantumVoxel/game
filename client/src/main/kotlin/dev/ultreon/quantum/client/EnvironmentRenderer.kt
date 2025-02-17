@file:Suppress("t")

package dev.ultreon.quantum.client

//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import com.github.tommyettinger.textra.Layout
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.client.gui.screens.IdScreen
import dev.ultreon.quantum.client.gui.screens.screen
import dev.ultreon.quantum.client.input.KeyBinds
import dev.ultreon.quantum.client.world.Skybox
import dev.ultreon.quantum.entity.PositionComponent
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.util.BlockHit
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.util.id
import dev.ultreon.quantum.vec3d
import kotlinx.coroutines.launch
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.async.KtxAsync
import ktx.math.vec3

private val tmp1 = vec3()

fun modelBatch() = ModelBatch(
  if (gamePlatform.isWebGL3 || gamePlatform.isGL30 || gamePlatform.isGLES3) {
    object : DefaultShaderProvider(
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/default.vsh")).text,
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/default.fsh")).text
    ) {
      override fun createShader(renderable: Renderable): Shader {
        return DefaultShader(
          renderable,
          this.config,
          "#version 300 es\n\n" + DefaultShader.createPrefix(renderable, config)
        )
      }
    }
  } else {
    object : DefaultShaderProvider(
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/legacy/default.vsh")).text,
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/legacy/default.fsh")).text
    ) {

    }
  },
  DefaultRenderableSorter()
)

/**
 * Represents the main game screen responsible for rendering and managing the player's interaction
 * with the game world. This class implements the `KtxScreen` interface and handles functionality
 * such as input handling, rendering, player movement, camera operations, and resource management.
 *
 * The `GameScreen` for example manages:
 * - Rendering 3D models and 2D overlays.
 * - Camera and viewport configurations.
 * - Input processing for movement, actions, and interaction.
 * - Player entity initialization and behavior updates.
 * - Environmental light and textures.
 *
 * @constructor Creates a `GameScreen` instance that initializes the game world, rendering environment,
 *              camera, player entity, and necessary assets.
 */
class EnvironmentRenderer : Disposable {
  private var refreshing: Boolean = false
  private var time: Float = 0f
  private var lastHit: BlockHit? = null
  private val backupMatrix: Matrix4 = Matrix4()
  internal var lastRefreshPosition: Vector3D = vec3d()
  private var lastRefreshTime: Long = 0
  private var lastPollTime: Long = 0

  private val speed: Float = 6f

  private val hud by screen(path = "hud")

  private val modelBatch = modelBatch()

  private val font = quantum.font
  private val spriteBatch = spriteBatch()
  val camera = perspectiveCamera {
    position.set(0f, 1.6f, 0f)
    near = 0.01f
    far = 500f
    update()
  }

  val vel = vec3(0f, 0f, 0f)

  var moveX = 0F
  var moveY = 0F

  var forward = false
  var backward = false
  var strafeLeft = false
  var strafeRight = false
  var up = false
  var down = false

  val environment = Environment().apply {
    set(ColorAttribute.createAmbientLight(1F, 1F, 1F, 1F))
  }

  val skybox = Skybox()

  init {
    Future.runAsync { dimension!!.refreshChunks(player!!.positionComponent.position) }.apply {
      onFailure = { logger.error("Failed to refresh chunks", it.stackTraceToString()) }
    }
  }

  /**
   * Renders the frame for the game world. This method is called every frame and handles all rendering
   * and game state updates based on the time delta between frames.
   *
   * @param delta A `Float` value representing the time elapsed since the last frame, used to scale time-dependent calculations.
   */
  fun render(delta: Float) {
    val player = player
    val dimension = dimension
    if (player == null || dimension == null) {
      logger.warn("Player or dimension is null")
      return
    }
    val position: PositionComponent = player.positionComponent

    clearScreen(red = 0F, green = 0F, blue = 0F)
    Gdx.gl.glDepthMask(false)
    skybox.render(camera, position.xRot)

    Gdx.gl.glDepthMask(true)

    modelBatch.begin(camera)
    dimension.render(modelBatch, camera)
    modelBatch.end()
    dimension.pollChunks()

    vel.set(0f, 0f, 0f)

    moveX = 0F
    moveY = 0F

    input()

    move()
    controllerMove()

    if (Gdx.input.isCursorCatched || gamePlatform.isMobile) {
      look(position)
    }

    move(position, delta)

    Future.runAsync {
      if (!refreshing && lastRefreshTime + 1000 < System.currentTimeMillis()) {
        if (position.position != lastRefreshPosition) {
          this@EnvironmentRenderer.refreshing = true
          dimension.refreshChunks(player.positionComponent.position)
          lastRefreshTime = System.currentTimeMillis()
          lastRefreshPosition = position.position.copy()
          this@EnvironmentRenderer.refreshing = false
        } else {
          lastRefreshTime = System.currentTimeMillis()
        }
      }
    }

    backupMatrix.set(spriteBatch.transformMatrix)
    spriteBatch.transformMatrix =
      spriteBatch.transformMatrix.scale(quantum.guiScale, quantum.guiScale, quantum.guiScale)

    try {
      quantum.guiRenderer.begin()
      renderHud(
        (Gdx.input.x / quantum.guiScale).toInt(),
        (Gdx.input.y / quantum.guiScale).toInt()
      )
      quantum.guiRenderer.end()
    } finally {
      spriteBatch.transformMatrix = backupMatrix
    }
  }

  private fun renderHud(mouseX: Int, mouseY: Int) {
    hud.render(quantum.guiRenderer, mouseX, mouseY, Gdx.graphics.deltaTime)
  }

  private fun rayCast(): BlockHit {
    val position = player!!.positionComponent
    val hit = dimension!!.rayCast(position.position.cpy().add(0.0, 1.6, 0.0), vec3d().also {
      val vec = tmp1
      position.lookVec(vec)
      it.set(vec.x, vec.y, vec.z)
    })
    this.lastHit = hit
    return hit
  }

  /**
   * Updates the position and velocity of the player based on the current movement inputs and delta time.
   *
   * @param position A `PositionComponent` representing the current position and rotation state of the entity.
   * @param delta A `Float` representing the time elapsed since the last frame, used for scaling movement.
   */
  private fun move(position: PositionComponent, delta: Float) {
//    when {
//      moveX > 0 -> position.xRot = max(position.xRot - 45 / (position.xHeadRot - position.xRot + 50), position.xRot - 90)
//      moveX < -0 -> position.xRot = min(position.xRot + 45 / (position.xRot - position.xHeadRot + 50), position.xRot + 90)
//      moveY != 0F && position.xRot > position.xHeadRot -> position.xRot = max(position.xRot - (45 / (position.xRot - position.xHeadRot)), position.xHeadRot)
//      moveY != 0F && position.xRot < position.xHeadRot -> position.xRot = min(position.xRot + (45 / (position.xHeadRot - position.xRot)), position.xHeadRot)
//    }

    tmpVec.set(moveX, 0f, -moveY).nor().scl(speed)/*.rotate(Vector3.X, position.yRot + 90)*/
      .rotate(Vector3.Y, position.xHeadRot + 90)

    var flight = 0F

    vel.set(tmpVec).add(0F, -flight, 0F)

    val collision = player!!.physicsComponent ?: return
    if (vel.x != 0F) collision.velocityX = vel.x.toDouble() / TPS
    if (up && collision.onGround) collision.velocityY = 0.4
    if (vel.z != 0F) collision.velocityZ = vel.z.toDouble() / TPS
  }

  /**
   * Updates the player's rotation and camera direction based on mouse movement.
   *
   * @param position A `PositionComponent` that represents the current position and rotation state of the player.
   */
  private fun look(position: PositionComponent) {
    val deltaX = if (gameInput.isMouseSupported) gameInput.mouseDeltaX else Gdx.input.deltaX.toFloat()
    val deltaY = if (gameInput.isMouseSupported) gameInput.mouseDeltaY else Gdx.input.deltaY.toFloat()

    position.xRot -= deltaX * 0.5f
    position.xHeadRot = position.xRot
    position.yRot += deltaY * 0.5f

    position.xRot = (position.xRot + 180) % 360 - 180
    position.yRot = position.yRot.coerceIn(-89.99F, 89.99F)

    position.lookVec(camera.direction)
    camera.update()
  }

  private fun input() {
    if (!Gdx.input.isCursorCatched && !gamePlatform.isMobile) return
    forward = KeyBinds.walkForwardsKey.isPressed()
    backward = KeyBinds.walkBackwardsKey.isPressed()
    strafeLeft = KeyBinds.walkLeftKey.isPressed()
    strafeRight = KeyBinds.walkRightKey.isPressed()
    up = KeyBinds.jumpKey.isPressed()
    down = KeyBinds.crouchKey.isPressed()

    val inv = player!!.inventoryComponent
    if (inv != null) {
      for ((i, key) in KeyBinds.hotbarKeys.withIndex()) {
        if (key.isJustPressed()) {
          inv.hotbarIndex = i
        }
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !gamePlatform.isMobile) {
      if (Gdx.input.isCursorCatched) {
        quantum.submit {
          quantum.showScreen(IdScreen.get(id(path = "pause")) ?: run {
            logger.error("No pause screen found")
            return@submit
          })
        }
        Gdx.input.isCursorCatched = false
      }
    }

    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !gamePlatform.isMobile) {
      if (Gdx.input.isCursorCatched) {
        rayCast().let {
          val collide = it.isCollide
          if (collide) {
            dimension!![it.point.x, it.point.y, it.point.z] = Blocks.air
          }
        }
      }
      Gdx.input.isCursorCatched = true
    }

    player!!.runningComponent.running =
      KeyBinds.runningKey.isPressed()

    if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
      Future.runAsync {
        dimension!!.rebuildAll()
      }
    }
  }

  /**
   * Renders various debugging and informational data as an overlay on the screen.
   *
   * This function displays the player's current position, movement states, rotation values,
   * camera details, and frames per second (FPS). It uses a `SpriteBatch` instance to draw each line of text.
   *
   * @param position The player's current position and rotation, represented by a `PositionComponent` instance.
   */
  private fun drawInfo(position: PositionComponent) {
    backupMatrix.set(spriteBatch.transformMatrix)
    spriteBatch.transformMatrix =
      spriteBatch.transformMatrix.scale(quantum.guiScale, quantum.guiScale, quantum.guiScale)

    val pos = player!!.positionComponent.position

    try {
      font.draw(
        spriteBatch,
        "[gold]X: [white]${pos.x}",
        10f,
        10f
      )
      font.draw(
        spriteBatch,
        "Y: ${pos.y}",
        10f,
        20f
      )
      font.draw(
        spriteBatch,
        "Z: ${pos.z}",
        10f,
        30f
      )

      font.draw(spriteBatch, "[gold]Forward: [white]$forward", 10f, 40f)
      font.draw(spriteBatch, "[gold]Backward: [white]$backward", 10f, 50f)
      font.draw(spriteBatch, "[gold]Strafe Left: [white]$strafeLeft", 10f, 60f)
      font.draw(spriteBatch, "[gold]Strafe Right: [white]$strafeRight", 10f, 70f)

      font.draw(
        spriteBatch,
        "[gold]X Rotation: [white]${player!!.positionComponent.xRot}",
        10f,
        110f
      )
      font.draw(
        spriteBatch,
        "[gold]Y Rotation: [white]${player!!.positionComponent.yRot}",
        10f,
        120f
      )
      font.draw(
        spriteBatch,
        "[gold]X Head Rotation: [white]${player!!.positionComponent.xHeadRot}",
        10f,
        130f
      )

      font.draw(
        spriteBatch,
        "[gold]Running: [white]${player!!.runningComponent.running}",
        10f,
        140f
      )

      font.draw(spriteBatch, "[gold]FPS:[white]${Gdx.graphics.framesPerSecond}", 10f, 150f)

      font.draw(spriteBatch, "[gold]Camera Position: [white]${camera.position}", 10f, 160f)
      font.draw(spriteBatch, "[gold]Camera Direction: [white]${camera.direction}", 10f, 170f)
      font.draw(spriteBatch, "[gold]Camera Up: [white]${camera.up}", 10f, 180f)
      font.draw(
        spriteBatch, "[gold]Direction: [white]" + when {
          position.yRot < -45 -> "Up"
          position.yRot > 45 -> "Down"
          position.xRot < -45 && position.xRot > -135 -> "Left"
          position.xRot > 45 && position.xRot < 135 -> "Right"
          position.xRot > 135 || position.xRot < -135 -> "Backward"
          else -> "Forward"
        }, 10f, 190f
      )

      font.draw(spriteBatch, "[gold]Velocity: [white]$vel", 10f, 200f)
      font.draw(spriteBatch, "[gold]Chunk Position: [white]${position.chunkPosition}", 10f, 210f)

      font.draw(spriteBatch, "[gold]Is Mobile: [white]${gamePlatform.isMobile}", 10f, 220f)

      font.draw(spriteBatch, "[gold]Gui Scale: [white]${quantum.guiScale}", 10f, 230f)
      font.draw(spriteBatch, "[gold]Intersected at: [white]${lastHit?.point}", 10f, 240f)
    } finally {
      spriteBatch.transformMatrix.set(backupMatrix)
    }
  }

  fun move() {
    if (!this.forward && !this.backward && !this.strafeLeft && !this.strafeRight) return
    if (this.forward) this.moveY += 1f
    if (this.backward) this.moveY -= 1f
    if (this.strafeLeft) this.moveX -= 1f
    if (this.strafeRight) this.moveX += 1f
  }

  fun controllerMove() {
    // TODO
  }

  override fun dispose() {
    modelBatch.disposeSafely()
    skybox.disposeSafely()
    spriteBatch.disposeSafely()
  }

  /**
   * Adjusts the camera viewport dimensions to match the new width and height of the window.
   *
   * This method ensures that the camera's viewport is updated whenever the game window is resized.
   * It invokes the parent class's `resize` method, recalculates the camera's viewport width and height
   * based on the provided dimensions, and updates the camera configuration.
   *
   * @param width The new width of the game window in pixels.
   * @param height The new height of the game window in pixels.
   */
  fun resize(width: Int, height: Int) {
    camera.viewportWidth = width.toFloat()
    camera.viewportHeight = height.toFloat()
    camera.update()

    spriteBatch.projectionMatrix =
      spriteBatch.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())

    hud.resize(
      width = (width / quantum.guiScale).toInt(),
      height = (height / quantum.guiScale).toInt()
    )
  }
}

private val layout = Layout()
