package dev.ultreon.quantum.client.world

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.Material
import dev.ultreon.quantum.InternalApi
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.client.*
import dev.ultreon.quantum.client.model.JsonModel
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.world.Element

interface TextureOffset {
  fun get(modelInfo: ModelInfo, texture: NamespaceID, jsonModel: JsonModel? = null): TextureRegion?
}

data class ModelInfo(
  val element: Element<*>
)

enum class GLPrimitiveType(val gl: Int) {
  Triangle(GL20.GL_TRIANGLES),
  TriangleFan(GL20.GL_TRIANGLE_FAN),
  TriangleStrip(GL20.GL_TRIANGLE_STRIP),
  Line(GL20.GL_LINES),
  LineStrip(GL20.GL_LINE_STRIP),
  LineLoop(GL20.GL_LINE_LOOP),
  Point(GL20.GL_POINTS);

}

data class RenderInfo(
  val name: String,
  val textureOffset: TextureOffset,
  val material: (texture: NamespaceID) -> Material,
  val primitiveType: GLPrimitiveType,
  val vertexAttributes: VertexAttributes,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RenderInfo

    return name == other.name
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  companion object {

    private val registry: MutableMap<String, RenderInfo> = mutableMapOf()

    operator fun get(name: String): RenderInfo {
      return registry[name] ?: throw NoSuchElementException("Render info '$name' not found")
    }

    fun register(name: String, info: RenderInfo): RenderInfo {
      if (name in registry) throw IllegalStateException("Duplicate element '$name'")

      registry[name] = info
      return info
    }

    val default = renderInfo("default") {
      textureOffset(BlockTextureOffset)
      material {
        baseColorTexture(texture(it))
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".normal.png" })?.let(::normalTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".emission.png" })?.let(::emissiveTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".mr.png" })?.let(::metallicRoughnessTexture)
      }
      vertexAttributes(
        VertexAttribute.Position(),
        VertexAttribute.ColorUnpacked(),
        VertexAttribute.Normal(),
        VertexAttribute.TexCoords(0),
      )
    }

    val water = renderInfo("water") {
      textureOffset(BlockTextureOffset)
      material {
        baseColorTexture(texture(it))
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".normal.png" })?.let(::normalTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".emission.png" })?.let(::emissiveTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".mr.png" })?.let(::metallicRoughnessTexture)

        dynamicReflect()
      }
      vertexAttributes(
        VertexAttribute.Position(),
        VertexAttribute.ColorUnpacked(),
        VertexAttribute.Normal(),
        VertexAttribute.TexCoords(0),
      )
    }

    val transparent = renderInfo("transparent") {
      textureOffset(BlockTextureOffset)
      material {
        baseColorTexture(texture(it))
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".normal.png" })?.let(::normalTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".emission.png" })?.let(::emissiveTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".mr.png" })?.let(::metallicRoughnessTexture)
        blendMode(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        alphaTest(0.01f)

        dynamicReflect()
      }
      vertexAttributes(
        VertexAttribute.Position(),
        VertexAttribute.ColorUnpacked(),
        VertexAttribute.Normal(),
        VertexAttribute.TexCoords(0),
      )
    }
    val cutout = renderInfo("cutout") {
      textureOffset(BlockTextureOffset)
      material {
        baseColorTexture(texture(it))
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".normal.png" })?.let(::normalTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".emission.png" })?.let(::emissiveTexture)
        textureOrNull(it.mapPath { path -> path.substringBeforeLast(".png") + ".mr.png" })?.let(::metallicRoughnessTexture)
        alphaTest(0.01f)

        dynamicReflect()
      }
      vertexAttributes(
        VertexAttribute.Position(),
        VertexAttribute.ColorUnpacked(),
        VertexAttribute.Normal(),
        VertexAttribute.TexCoords(0),
      )
    }
  }
}

enum class TextureType {
  BaseColor,
  MetallicRoughness,
  Normal,
  Bump
}

object BlockTextureOffset : TextureOffset {
  override fun get(modelInfo: ModelInfo, texture: NamespaceID, jsonModel: JsonModel?): TextureRegion {
    val value = modelInfo.element.value
    if (value is Block) {
      return TextureRegion(quantum.blockAtlas)
    }

    throw IllegalStateException("Not a block")
  }
}

class RenderInfoBuilder {
  private lateinit var vertexAttributes: VertexAttributes
  private lateinit var offset: TextureOffset
  private lateinit var material: (texture: NamespaceID) -> Material
  private var primitiveType: GLPrimitiveType = GLPrimitiveType.Triangle

  fun textureOffset(offset: TextureOffset) {
    this.offset = offset
  }

  @OptIn(InternalApi::class)
  fun material(builder: MaterialBuilder.(texture: NamespaceID) -> Unit) {
    material = { texture -> MaterialBuilderImpl().apply { builder(this, texture) }.build() }
  }

  fun primitiveType(primitiveType: GLPrimitiveType) {
    this.primitiveType = primitiveType
  }

  fun vertexAttributes(vertexAttributes: VertexAttributes) {
    this.vertexAttributes = vertexAttributes
  }

  fun vertexAttributes(vararg attrs: VertexAttribute) {
    this.vertexAttributes = VertexAttributes(*attrs)
  }

  @InternalApi
  fun build(name: String): RenderInfo {
    return RenderInfo(name, offset, material, primitiveType, vertexAttributes)
  }
}

@OptIn(InternalApi::class)
inline fun renderInfo(name: String, crossinline builder: RenderInfoBuilder.() -> Unit): RenderInfo {
  return RenderInfoBuilder().apply(builder).build(name).also {
    RenderInfo.register(name, it)
  }
}
