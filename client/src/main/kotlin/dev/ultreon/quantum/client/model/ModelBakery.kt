package dev.ultreon.quantum.client.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import dev.ultreon.quantum.client.world.RenderInfo
import dev.ultreon.quantum.util.NamespaceID

class ModelBakery(private val modelBuilder: ModelBuilder) {
  private val renderData = mutableMapOf<Pair<RenderInfo, NamespaceID>, MeshPartBuilder>()

  fun node(renderInfo: RenderInfo, texture: NamespaceID): MeshPartBuilder {
    return renderData[renderInfo to texture] ?: modelBuilder.part(renderInfo.name, renderInfo.primitiveType.gl, renderInfo.vertexAttributes, renderInfo.material(texture)).also {
      renderData[renderInfo to texture] = it
    }
  }

  fun end(): Model? {
    return modelBuilder.end()
  }
}
