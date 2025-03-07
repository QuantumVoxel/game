package dev.ultreon.quantum.client.model

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.client.world.AOArray
import dev.ultreon.quantum.util.NamespaceID
import kotlinx.coroutines.runBlocking
import ktx.assets.disposeSafely

/**
 * Represents a model of a block.
 *
 * @property isCustom Whether the model is custom or not.
 * @property model The model of the block.
 * @property itemScale The scale of the item.
 * @property itemOffset The offset of the item.
 * @property buriedTexture The texture when the player is buried inside the block.
 */
@Suppress("unused")
interface BlockModel : LoadableResource, Disposable {
  val isCustom: Boolean

  /**
   * Bakes the model into the specified [builder] at the specified [x], [y], and [z] coordinates.
   * The model will be culled based on the specified [faceCull].
   * And the ambient occlusion will be calculated based on the specified [aoArray].
   *
   * @param builder The mesh part builder to load the model into.
   * @param x The x coordinate of the model.
   * @param y The y coordinate of the model.
   * @param z The z coordinate of the model.
   * @param faceCull The face culling of the model.
   * @param aoArray The ambient occlusion array.
   */
  suspend fun loadIntoAsync(builder: MeshPartBuilder, x: Int, y: Int, z: Int, faceCull: FaceCull, aoArray: AOArray) {
    // Do nothing
  }

  val model: Model?

  /**
   * Disposes of the model.
   */
  override fun dispose() {
    model?.disposeSafely()
  }

  /**
   * Gets the resource id of the model.
   *
   * @return The resource id of the model.
   */
  fun resourceId(): NamespaceID?

  val itemScale: Vector3
    get() = Vector3(0.0625f, 0.0625f, 0.0625f)

  val itemOffset: Vector3
    get() = Vector3(0f, -20f, 0f)

  val buriedTexture: TextureRegion?
    get() = null

  /**
   * Represents the face culling of a model.
   *
   * @property defaultItemScale The default scale of the item.
   */
  companion object {
    val defaultItemScale: Vector3 = Vector3(1f, 1f, 1f)
  }

  /**
   * Bakes the model into the specified [builder] at the specified [x], [y], and [z] coordinates.
   * The model will be culled based on the specified [faceCull].
   * And the ambient occlusion will be calculated based on the specified [aoArray].
   *
   * @param builder The mesh part builder to load the model into.
   * @param x The x coordinate of the model.
   * @param y The y coordinate of the model.
   * @param z The z coordinate of the model.
   * @param faceCull The face culling of the model.
   * @param aoArray The ambient occlusion array.
   */
  fun loadInto(builder: MeshPartBuilder, x: Int, y: Int, z: Int, faceCull: FaceCull, aoArray: AOArray) {
    runBlocking {
      loadIntoAsync(builder, x, y, z, faceCull, aoArray)
    }
  }
}
