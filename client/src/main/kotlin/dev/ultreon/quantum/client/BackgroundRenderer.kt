package dev.ultreon.quantum.client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.client.world.ClientChunk
import dev.ultreon.quantum.client.world.ClientDimension
import dev.ultreon.quantum.client.world.Skybox
import dev.ultreon.quantum.gamePlatform
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.vec3d
import dev.ultreon.quantum.world.BlockFlags
import dev.ultreon.quantum.world.SIZE
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random
import kotlin.random.nextInt

class BackgroundRenderer : Disposable {
  private val tmp: Vector3D = vec3d()
  private val camera = perspectiveCamera {
    position.set(0f, 0f, 0f)
    up.set(0f, 1f, 0f)
    lookAt(0f, 0f, 0f)
    fieldOfView = 70f
    near = 0.1f
    far = 100f
    update()
  }
  private val modelBatch = ModelBatch(
    if (gamePlatform.isGL30 || gamePlatform.isGLES3 || gamePlatform.isWebGL3) {
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/default.vsh")).text
    } else {
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/legacy/default.vsh")).text
    },
    if (gamePlatform.isGL30 || gamePlatform.isGLES3 || gamePlatform.isWebGL3) {
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/default.fsh")).text
    } else {
      (quantum.clientResources require NamespaceID.of(path = "shaders/programs/legacy/default.fsh")).text
    }
  )
  private val skybox: Skybox = Skybox()
  private var chunks = Array(3) { ClientChunk(it - 1, 0, 0, quantum.material, FakeDimension()) }

  init {
    val selector = Random.nextInt(4)
    for (chunk in chunks) when (selector) {
      0 -> {
        // Create a platform
        for (x in 0 until SIZE) {
          for (z in 0 until SIZE) {
            chunk.set(x, 0, z, Blocks.grass, BlockFlags.NONE)
          }
        }

        Blocks.crate?.let { crate ->
          repeat(16) {
            val x = Random.nextInt(0..SIZE)
            val z = Random.nextInt(0..SIZE)
            chunk.set(x, 1, z, crate, BlockFlags.NONE)
          }
        }

        chunk.rebuild()
      }

      1 -> {
        // Create a platform
        for (x in 0 until SIZE) {
          for (z in 0 until SIZE) {
            chunk.set(x, 0, z, Blocks.sand ?: Blocks.grass, BlockFlags.NONE)
          }
        }

        chunk.rebuild()
      }

      2 -> {
        // Create a platform
        Blocks.iron?.let {
          for (x in 0 until SIZE) {
            for (z in 0 until SIZE) {
              chunk.set(x, 0, z, it, BlockFlags.NONE)
            }
          }
        } ?: run {
          for (x in 0 until SIZE) {
            for (z in 0 until SIZE) {
              chunk.set(x, 0, z, Blocks.stone, BlockFlags.NONE)
            }
          }
        }

        chunk.rebuild()
      }

      3 -> {
        // Create a platform
        for (x in 0 until SIZE) {
          for (z in 0 until SIZE) {
            chunk.set(x, 0, z, Blocks.sand ?: Blocks.grass, BlockFlags.NONE)
            chunk.set(x, 1, z, Blocks.water, BlockFlags.NONE)
            chunk.set(x, 2, z, Blocks.water, BlockFlags.NONE)
          }
        }

        chunk.rebuild()
      }

      else -> {}
    }
  }

  fun render() {
    camera.viewportWidth = Gdx.graphics.width.toFloat()
    camera.viewportHeight = Gdx.graphics.height.toFloat()
    camera.update()
    val d1 = (-Gdx.input.y.toFloat() / Gdx.graphics.height - 0.5) / 4.0
    val d2 = (Gdx.input.x.toFloat() / Gdx.graphics.width - 0.5) / 4.0
    val sig1 = sign(d1)
    val sig2 = sign(d2)
    tmp.set(
      SIZE / 2f + abs(d2 * d2).pow(0.5) * sig2,
      4f + abs(d1 * d1).pow(0.5) * sig1,
      SIZE / 2.0
    )
    modelBatch.begin(camera)
    Gdx.gl.glDepthMask(false)
    skybox.render(camera, 0f)
    modelBatch.flush()
    Gdx.gl.glDepthMask(true)
    for (chunk in chunks) {
      chunk.reposition(tmp)
      modelBatch.render(chunk)
    }
    modelBatch.end()
  }

  override fun dispose() {
    modelBatch.dispose()
    for (chunk in chunks) {
      chunk.disposeChunk()
    }

    skybox.dispose()
  }
}

class FakeDimension : ClientDimension(quantum.material)
