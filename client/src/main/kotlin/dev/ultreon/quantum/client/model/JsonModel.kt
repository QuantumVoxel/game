package dev.ultreon.quantum.client.model

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3
import dev.ultreon.quantum.client.model.JsonModelLoader.ModelElement
import dev.ultreon.quantum.client.world.AOArray
import dev.ultreon.quantum.util.NamespaceID
import kotlinx.coroutines.yield
import ktx.assets.disposeSafely
import java.util.function.Consumer

class JsonModel(
  val id: NamespaceID,
  private val textureElements: Map<String, NamespaceID>,
  private val modelElements: List<ModelElement>,
  val ambientOcclusion: Boolean,
  val display: JsonModelLoader.Display,
) : BlockModel, ItemModel {

  override var model: Model? = null
    private set

  override fun bake(): Model {
    return generateModel(id) { modelBuilder ->
      var i = 0
      val modelElementsSize = modelElements.size
      while (i < modelElementsSize) {
        val modelElement: ModelElement = modelElements[i]
        modelElement.bake(i, modelBuilder, textureElements)
        i++
      }
    }
  }

  override fun loadInto(builder: MeshPartBuilder, x: Int, y: Int, z: Int, faceCull: FaceCull, aoArray: AOArray) {
    var i = 0
    val modelElementsSize = modelElements.size
    while (i < modelElementsSize) {
      val modelElement: ModelElement = modelElements[i]
      modelElement.loadInto(i, faceCull, aoArray, x, y, z, builder, textureElements)
      i++
    }
  }

  override suspend fun loadIntoAsync(
    builder: MeshPartBuilder,
    x: Int,
    y: Int,
    z: Int,
    faceCull: FaceCull,
    aoArray: AOArray
  ) {
    var i = 0
    val modelElementsSize = modelElements.size
    while (i < modelElementsSize) {
      val modelElement: ModelElement = modelElements[i]
      modelElement.loadInto(i, faceCull, aoArray, x, y, z, builder, textureElements)
      i++
    }
  }

  fun load() {
    this.model = bake()
  }

  override val isCustom: Boolean
    get() = true

  override fun dispose() {
    model.disposeSafely()
  }

  override fun resourceId(): NamespaceID? {
    TODO("Not yet implemented")
  }

  override val itemOffset: Vector3
    get() = Vector3(0f, -20f, 0f)

  companion object {
    val itemScale: Vector3 = Vector3(0.0625f, 0.0625f, 0.0625f)
  }
}

private fun generateModel(id: NamespaceID, consumer: Consumer<ModelBuilder>): Model {
  val modelBuilder = ModelBuilder()
  consumer.accept(modelBuilder)
  return modelBuilder.end()
}
