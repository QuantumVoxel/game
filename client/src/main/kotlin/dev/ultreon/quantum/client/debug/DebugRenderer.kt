package dev.ultreon.quantum.client.debug

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import dev.ultreon.quantum.client.draw
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.client.world.LocalPlayer
import dev.ultreon.quantum.client.world.allLoading
import dev.ultreon.quantum.entity.PhysicsComponent
import dev.ultreon.quantum.entity.PositionComponent
import dev.ultreon.quantum.entity.RunningComponent
import ktx.graphics.use
import java.util.*

private val runtime: Runtime = Runtime.getRuntime()

class DebugRenderer {
  var line = 1
  var page = 0
    private set

  fun render() {
    line = 1

    if (!quantum.debug) {
      return
    }

    quantum.globalBatch.use {
      val player: LocalPlayer? = quantum.player
      when (page) {
        0 -> drawDebugPage1(it, player)
        1 -> drawDebugPage2(it, player)
        2 -> drawDebugPage3(it, player)
        3 -> drawDebugPage4(it, player)
        4 -> drawDebugPage5(it, player)
        5 -> drawDebugPage6(it, player)
      }
    }
  }

  fun drawDebugPage1(batch: SpriteBatch, player: LocalPlayer?) {
    if (player != null) {
      val position: PositionComponent? = player.positionComponent
      if (position != null) {
        left("📍", "XYZ", position.position)
        left("🔄", "X Rotation", "${position.xRot}, ${position.yRot}")
      }

      val running: RunningComponent? = player.runningComponent
      if (running != null) {
        left("💨", "Running", running.running)
      }

      val collision: PhysicsComponent? = player.physicsComponent
      if (collision != null) {
        left("🌊", "On Ground", collision.onGround)
        left("👉", "Collide XYZ", "${collision.isCollidingX}, ${collision.isCollidingY}, ${collision.isCollidingZ}")
        left("👉", "Colliding", collision.isColliding)
        left("👻", "No Clip", collision.noClip)
      }
    } else {
      left("👻", "No Player", null)
    }

    val memory = runtime.totalMemory() - runtime.freeMemory()
    val mb = memory / 1024.0 / 1024.0
    left("💾", "Used Memory", "${String.format(Locale.getDefault(), "%.2f", mb)} MB")
    left(
      "💾",
      "Total Memory",
      "${String.format(Locale.getDefault(), "%.2f", runtime.totalMemory() / 1024.0 / 1024.0)} MB"
    )
    left("🕒", "FPS", Gdx.graphics.framesPerSecond)

    left("📦", "Mesh Status", Mesh.getManagedStatus())
    left("📦", "Shader Status", ShaderProgram.getManagedStatus())
    left("📦", "Texture Status", Texture.getManagedStatus())
    left("📦", "Texture 3D Status", Texture3D.getManagedStatus())
    left("📦", "Texture Array Status", TextureArray.getManagedStatus())
    left("📦", "Framebuffer Status", GLFrameBuffer.getManagedStatus())
    left("📦", "Cubemap Status", Cubemap.getManagedStatus())

    left("📥", "Loading chunks count", allLoading)

    // Input
    left("🖱️", "Mouse X", Gdx.input.x)
    left("🖱️", "Mouse Y", Gdx.input.y)
    left("🖱️", "Input Processor", Gdx.input.inputProcessor?.javaClass?.simpleName ?: "None")
    left("🖱️", "Cursor Catched", Gdx.input.isCursorCatched)
    left("🖱️", "Mouse Delta X", Gdx.input.deltaX)
    left("🖱️", "Mouse Delta Y", Gdx.input.deltaY)
    for (i in 0 until Gdx.input.maxPointers) {
      if (!Gdx.input.isTouched(i)) continue
      left("🖱️", "Pointer $i", Gdx.input.isTouched(i))
      left("🖱️", "Pointer $i Delta X", Gdx.input.getDeltaX(i))
      left("🖱️", "Pointer $i Delta Y", Gdx.input.getDeltaY(i))
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
      page = (page + 1) % 6
    }
  }

  fun drawDebugPage2(batch: SpriteBatch, player: LocalPlayer?) {
    left("📦", "Chunk Queue Size", quantum.chunkQueue)
  }

  fun drawDebugPage3(batch: SpriteBatch, player: LocalPlayer?) {
    left("📦", "Chunk Queue Size", quantum.chunkQueue)
  }

  fun drawDebugPage4(batch: SpriteBatch, player: LocalPlayer?) {
    left("📦", "Chunk Queue Size", quantum.chunkQueue)
  }

  fun drawDebugPage5(batch: SpriteBatch, player: LocalPlayer?) {
    left("📦", "Chunk Queue Size", quantum.chunkQueue)
  }

  fun drawDebugPage6(batch: SpriteBatch, player: LocalPlayer?) {
    left("📦", "Chunk Queue Size", quantum.chunkQueue)
  }

  fun left(name: String, value: Any?) {
    quantum.font.draw(quantum.globalBatch, "[gold]$name: [white]$value", 10f, 10f + (line++ * 10f))
  }

  fun left(emoji: String, name: String, value: Any?) {
    quantum.font.draw(quantum.globalBatch, "[+$emoji][gold]$name: [white]$value", 10f, 10f + (line++ * 10f))
  }
}
