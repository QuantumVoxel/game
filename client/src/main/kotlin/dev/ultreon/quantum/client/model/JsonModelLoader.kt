package dev.ultreon.quantum.client.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Quaternion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.client.world.AOArray
import dev.ultreon.quantum.client.world.ModelInfo
import dev.ultreon.quantum.client.world.RenderInfo
import dev.ultreon.quantum.id
import dev.ultreon.quantum.item.Item
import dev.ultreon.quantum.key
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.math.Axis
import dev.ultreon.quantum.math.toVector
import dev.ultreon.quantum.registry.RegistryKeys
import dev.ultreon.quantum.resource.Resource
import dev.ultreon.quantum.resource.ResourceId
import dev.ultreon.quantum.resource.ResourceManager
import dev.ultreon.quantum.util.Direction
import dev.ultreon.quantum.util.Direction.*
import dev.ultreon.quantum.util.NamespaceID
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute
import java.io.IOException
import java.util.*

@JvmInline
value class FaceCull(val data: Int) {
  constructor(front: Boolean, back: Boolean, left: Boolean, right: Boolean, top: Boolean, bottom: Boolean) : this(
    (if (front) 1 else 0) or
      (if (back) 2 else 0) or
      (if (left) 4 else 0) or
      (if (right) 8 else 0) or
      (if (top) 16 else 0) or
      (if (bottom) 32 else 0)
  )

  fun front(): Boolean = data and 0b00000001 == 1

  fun back(): Boolean = data and 0b00000010 == 2

  fun left(): Boolean = data and 0b00000100 == 4

  fun right(): Boolean = data and 0b00001000 == 8

  fun top(): Boolean = data and 0b00010000 == 16

  fun bottom(): Boolean = data and 0b00100000 == 32

  fun face(dir: Direction) = when (dir) {
    NORTH -> front()
    SOUTH -> back()
    WEST -> left()
    EAST -> right()
    UP -> top()
    DOWN -> bottom()
  }
}

class JsonModelLoader @JvmOverloads constructor(
  private val resourceManager: ResourceManager = quantum.clientResources,
) {
  private var key: ResourceId<*>? = null

  @Throws(IOException::class)
  fun load(block: Block): JsonModel? {
    val namespaceID: NamespaceID = block.id.mapPath { path -> "models/blocks/$path.quant" }
    try {
      val resource: Resource = resourceManager[namespaceID] ?: run {
        logger.warn("No blocks model for ${block.id}")
        return null
      }
      val jsonReader = JsonReader()
      val inputStream = resource.inputStream()
      val modelData = jsonReader.parse(inputStream)
      val key1 = block.key
      val load = this.load(key1, modelData)
      inputStream.close()
      return load
    } catch (e: IOException) {
      logger.error("Couldn't load blocks model for ${block.id}: ${e.message}")
      return null
    }
  }

  @Throws(IOException::class)
  fun load(item: Item): JsonModel? {
    val namespaceID: NamespaceID = item.id.mapPath { path -> "models/items/$path.quant" }
    try {
      val resource: Resource = resourceManager[namespaceID] ?: run {
        logger.warn("No item model for ${item.id}")
        return null
      }
      val modelData = JsonReader().parse(resource.inputStream())
      val load = this.load(item.key, modelData)
      return load
    } catch (e: IOException) {
      logger.error("Couldn't load item model for ${item.id}: ${e.message}")
      return null
    }
  }

  @Suppress("SpellCheckingInspection")
  fun load(key: ResourceId<*>, modelData: JsonValue): JsonModel {
    require(
      !(key.parent!! != RegistryKeys.blocks && key.parent!! != RegistryKeys.items)
    ) { "Invalid model key, must be blocks or item: $key" }

    val root: JsonValue = modelData
    val textures: JsonValue = root["textures"]
    val textureElements: Map<String, NamespaceID> = loadTextures(textures)

    //        GridPoint2 textureSize = loadVec2i(root.getAsJson5Array("texture_size"), new GridPoint2(16, 16));
    val textureSize = GridPoint2(16, 16)

    val elements: JsonValue = root["elements"]

    val modelElements = loadElements(elements, textureSize.x, textureSize.y)

    val ambientOcclusion: Boolean = root.getBoolean("ambientocclusion", true)

    // TODO: Allow display properties.
    val display = Display()

    return JsonModel(key.name, textureElements, modelElements, ambientOcclusion, display)
  }

  private fun loadVec2i(textureSize: JsonValue, defaultValue: GridPoint2): GridPoint2 {
    require(textureSize.size == 2) { "Invalid 'texture_size' array at ${textureSize.trace()}: $textureSize" }
    return GridPoint2(textureSize.get(0).asInt(), textureSize.get(1).asInt())
  }

  private fun loadElements(elements: JsonValue, textureWidth: Int, textureHeight: Int): List<ModelElement> {
    val modelElements: MutableList<ModelElement> = ArrayList()

    for (elem in elements) {
      val element: JsonValue =
        if (elem.isObject) elem else throw IOException("Invalid element at ${elem.trace()}: $elem")
      val faces: JsonValue = element.get("faces")
      val blockFaceFaceElementMap: Map<Direction, FaceElement> = loadFaces(faces, textureWidth, textureHeight)

      val shade1: JsonValue? = element.get("shade")
      val shade = shade1 != null && shade1.asBoolean()
      val rotation1: JsonValue? = element.get("rotation")
      val rotation = ElementRotation.deserialize(rotation1)

      val from = loadVec3(element.get("from").apply {
        if (!isArray) throw IOException("Invalid 'from' array at ${element.trace()}: $element")
      })
      val to = loadVec3(element.get("to").apply {
        if (!isArray) throw IOException("Invalid 'to' array at ${element.trace()}: $element")
      })

      val modelElement = ModelElement(blockFaceFaceElementMap, shade, rotation, from, to)
      modelElements.add(modelElement)
    }

    return modelElements
  }

  private fun loadVec3(from: JsonValue): Vector3 =
    Vector3(from.get(0).asFloat(), from.get(1).asFloat(), from.get(2).asFloat())

  @Suppress("SpellCheckingInspection")
  private fun loadFaces(faces: JsonValue, textureWidth: Int, textureHeight: Int): Map<Direction, FaceElement> {
    val faceElems: MutableMap<Direction, FaceElement> = EnumMap(Direction::class.java)
    for (e in faces) {
      val key1: String = e.name ?: throw IOException("Invalid face at ${e.trace()}: $e")
      val value: JsonValue = if (e.isObject) e else throw IOException("Invalid face at ${e.trace()}: $e")
      val direction: Direction = Direction.valueOf(key1.uppercase())
      val uvs = value.get("uv")?.asFloatArray() ?: throw IOException("Invalid 'uv' array at ${value.trace()}: $value")
      val texture: String =
        value.get("texture")?.asString() ?: throw IOException("Invalid 'texture' value at ${value.trace()}: $value")
      val rotation = value.get("rotation")?.asInt() ?: 0
      val tintIndex = value.get("tintindex")?.asInt() ?: 0
      val cullFace = value.get("cullface")?.asString()

      faceElems[direction] = FaceElement(
        texture, UVs(
          uvs[0], uvs[1], uvs[2], uvs[3], textureWidth, textureHeight
        ), rotation, tintIndex, cullFace
      )
    }

    return faceElems
  }

  private fun loadTextures(textures: JsonValue): Map<String, NamespaceID> {
    val textureElements: MutableMap<String, NamespaceID> = HashMap<String, NamespaceID>()

    for (entry in textures) {
      val name: String = entry.name!!
      val stringId: String = entry.asString()
      val id: NamespaceID = NamespaceID.parse(stringId).mapPath { path -> "textures/$path.png" }
      textureElements[name] = id
    }

    return textureElements
  }

  fun load(key: ResourceId<*>, id: NamespaceID): BlockModel? {
    return try {
      val resource: Resource = resourceManager[id.mapPath { path -> "models/${key.name.path}/$path.quant" }] ?: return null
      this.load(key, JsonReader().parse(resource.inputStream()))
    } catch (e: IOException) {
      null
    }
  }

  @Suppress("SpellCheckingInspection")
  class FaceElement(
    val texture: String, val uvs: UVs, private val rotation: Int, private val tintindex: Int,
    private val cullface: String?,
  ) {
    fun texture(): String = texture

    fun uvs(): UVs = uvs

    fun rotation(): Int = rotation

    fun tintindex(): Int = tintindex

    fun cullface(): String? = cullface


    override fun toString(): String =
      "FaceElement[texture=$texture, uvs=$uvs, rotation=$rotation, tintindex=$tintindex, cullface=$cullface]"

    fun loadInto(i: Int, builder: MeshPartBuilder, textureElements: Map<String, NamespaceID>) {
      val faceElement: FaceElement = this
      val texture: String = faceElement.texture()
      val uvs: UVs = faceElement.uvs()
      val rotation: Int = faceElement.rotation()
      val tintindex: Int = faceElement.tintindex()
      val cullface: String? = faceElement.cullface()

      val textureId: NamespaceID = textureElements[texture]!!


    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is FaceElement) return false

      if (rotation != other.rotation) return false
      if (tintindex != other.tintindex) return false
      if (texture != other.texture) return false
      if (uvs != other.uvs) return false
      if (cullface != other.cullface) return false

      return true
    }

    override fun hashCode(): Int {
      var result = rotation
      result = 31 * result + tintindex
      result = 31 * result + texture.hashCode()
      result = 31 * result + uvs.hashCode()
      result = 31 * result + (cullface?.hashCode() ?: 0)
      return result
    }

    fun cullfaceDir(): Direction? {
      return cullface?.let { Direction.valueOf(it.uppercase()) }
    }
  }

  class UVs {
    val x1: Float
    val y1: Float
    val x2: Float
    val y2: Float

    constructor(x1: Float, y1: Float, x2: Float, y2: Float) {
      this.x1 = x1 / 16.0f
      this.y1 = y1 / 16.0f
      this.x2 = x2 / 16.0f
      this.y2 = y2 / 16.0f
    }

    constructor(x1: Float, y1: Float, x2: Float, y2: Float, textureWidth: Int, textureHeight: Int) {
      this.x1 = x1 / textureWidth
      this.y1 = y1 / textureHeight
      this.x2 = x2 / textureWidth
      this.y2 = y2 / textureHeight
    }

    fun x1(): Float = x1

    fun y1(): Float = y1

    fun x2(): Float = x2

    fun y2(): Float = y2

    override fun toString(): String = "UVs[x1=$x1, y1=$y1, x2=$x2, y2=$y2]"

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is UVs) return false

      if (x1 != other.x1) return false
      if (y1 != other.y1) return false
      if (x2 != other.x2) return false
      if (y2 != other.y2) return false

      return true
    }

    override fun hashCode(): Int {
      var result = x1.hashCode()
      result = 31 * result + y1.hashCode()
      result = 31 * result + x2.hashCode()
      result = 31 * result + y2.hashCode()
      return result
    }
  }

  class ModelElement(
    private val blockFaceFaceElementMap: Map<Direction, FaceElement>,
    private val shade: Boolean,
    private val rotation: ElementRotation,
    private val from: Vector3,
    private val to: Vector3,
  ) {
    fun bake(idx: Int, modelBuilder: ModelBuilder, textureElements: Map<String, NamespaceID>) {
      val from = this.from()
      val to = this.to()

      val nodeBuilder = ModelBuilder()
      nodeBuilder.begin()

      val meshBuilder = MeshBuilder()
      val v00 = VertexInfo()
      val v01 = VertexInfo()
      val v10 = VertexInfo()
      val v11 = VertexInfo()
      for ((direction, faceElement) in blockFaceFaceElementMap) {
        val texRef = faceElement.texture
        val texture: NamespaceID? = if (texRef == "#missing") NamespaceID.of(path = "textures/blocks/error.png")
        else if (texRef.startsWith("#")) textureElements[texRef.substring(1)]
        else NamespaceID.parse(texRef).mapPath { path -> "textures/$path.png" }

        meshBuilder.begin(
          VertexAttributes(
            VertexAttribute.Position(),
            VertexAttribute.ColorUnpacked(),
            VertexAttribute.Normal(),
            VertexAttribute.TexCoords(0)
          ), GL20.GL_TRIANGLES
        )
        v00.setCol(Color.WHITE)
        v01.setCol(Color.WHITE)
        v10.setCol(Color.WHITE)
        v11.setCol(Color.WHITE)

        v00.setNor(direction.normal)
        v01.setNor(direction.normal)
        v10.setNor(direction.normal)
        v11.setNor(direction.normal)

        val region = quantum.textureManager[texture!!]

        v00.setUV(faceElement.uvs.x1 / 16 + region.u, faceElement.uvs.y2 / 16 + region.v2)
        v01.setUV(faceElement.uvs.x1 / 16 + region.u, faceElement.uvs.y1 / 16 + region.v)
        v10.setUV(faceElement.uvs.x2 / 16 + region.u2, faceElement.uvs.y2 / 16 + region.v2)
        v11.setUV(faceElement.uvs.x2 / 16 + region.u2, faceElement.uvs.y1 / 16 + region.v)

        var dir: Byte = -1
        when (direction) {
          UP -> {
            v01.setPos(to.x, to.y, from.z)
            v00.setPos(to.x, to.y, to.z)
            v11.setPos(from.x, to.y, from.z)
            v10.setPos(from.x, to.y, to.z)
            dir = 0
          }

          DOWN -> {
            v00.setPos(from.x, from.y, from.z)
            v01.setPos(from.x, from.y, to.z)
            v10.setPos(to.x, from.y, from.z)
            v11.setPos(to.x, from.y, to.z)
            dir = 1
          }

          WEST -> {
            v00.setPos(from.x, from.y, from.z)
            v01.setPos(from.x, to.y, from.z)
            v10.setPos(from.x, from.y, to.z)
            v11.setPos(from.x, to.y, to.z)
            dir = 2
          }

          EAST -> {
            v00.setPos(to.x, from.y, to.z)
            v01.setPos(to.x, to.y, to.z)
            v10.setPos(to.x, from.y, from.z)
            v11.setPos(to.x, to.y, from.z)
            dir = 3
          }

          NORTH -> {
            v00.setPos(to.x, from.y, from.z)
            v01.setPos(to.x, to.y, from.z)
            v10.setPos(from.x, from.y, from.z)
            v11.setPos(from.x, to.y, from.z)
            dir = 4
          }

          SOUTH -> {
            v00.setPos(from.x, from.y, to.z)
            v01.setPos(from.x, to.y, to.z)
            v10.setPos(to.x, from.y, to.z)
            v11.setPos(to.x, to.y, to.z)
            dir = 5
          }
        }

        rotate(v00, v01, v10, v11, dir, rotation)

        meshBuilder.rect(v00, v10, v11, v01)

        val material = Material()
        material.set(PBRTextureAttribute.createBaseColorTexture(quantum.textureManager[texture]))
        material.set(BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA))
        material.set(FloatAttribute(FloatAttribute.AlphaTest))
        material.set(DepthTestAttribute(GL20.GL_LEQUAL))
        nodeBuilder.part(idx.toString() + "." + direction.name, meshBuilder.end(), GL20.GL_TRIANGLES, material)
      }

      val end = nodeBuilder.end()
      val node = modelBuilder.node()

      val originVec = rotation.originVec
      val axis: Axis = rotation.axis
      val angle = rotation.angle
      val rescale = rotation.rescale // TODO: implement

      node.localTransform.translate(originVec.x, originVec.y, originVec.z)
      node.localTransform.rotate(axis.toVector(), angle)
      node.localTransform.translate(-originVec.x, -originVec.y, -originVec.z)
      node.scale.set(node.localTransform.getScale(tmp))
      node.translation.set(node.localTransform.getTranslation(tmp))
      node.rotation.set(node.localTransform.getRotation(tmpQ))
    }

    private fun rotate(
      v00: VertexInfo,
      v01: VertexInfo,
      v10: VertexInfo,
      v11: VertexInfo,
      direction: Byte,
      rotation: ElementRotation,
    ): Boolean {
      val originVec = rotation.originVec
      val axis = rotation.axis
      val angle = rotation.angle
      val rescale = rotation.rescale // TODO: implement

      // Rotate the vertices
      rotate(v00.position, originVec, axis, angle, v00.position)
      rotate(v01.position, originVec, axis, angle, v01.position)
      rotate(v10.position, originVec, axis, angle, v10.position)
      rotate(v11.position, originVec, axis, angle, v11.position)

      return true
    }

    fun rotate(position: Vector3, originVec: Vector3, axis: Axis, degrees: Float, out: Vector3): Vector3 {
      return rotateVector(position, originVec, degrees, axis.toVector(), out)
    }

    private val aoColor: Color = Color(0.5f, 0.5f, 0.5f, 1f)

    // Reusable instances
    private val tempVector: Vector3 = Vector3()

    private val rotationMatrix: Matrix4 = Matrix4()

    private fun rotateVector(
      vector: Vector3,
      origin: Vector3,
      angleDegrees: Float,
      axis: Vector3,
      result: Vector3,
    ): Vector3 {
      // Step 1: Translate the vector to the origin
      tempVector.set(vector).sub(origin)

      // Step 2: Create a rotation matrix
      rotationMatrix.setToRotation(axis, angleDegrees)

      // Step 3: Apply the rotation matrix to the translated vector
      tempVector.mul(rotationMatrix)

      // Step 4: Translate the vector back
      result.set(tempVector).add(origin)

      return result
    }

    fun blockFaceFaceElementMap(): Map<Direction, FaceElement> = blockFaceFaceElementMap

    fun shade(): Boolean = shade

    fun rotation(): ElementRotation = rotation

    fun from(): Vector3 = from

    fun to(): Vector3 = to


    override fun toString(): String =
      "ModelElement[blockFaceFaceElementMap=$blockFaceFaceElementMap, shade=$shade, rotation=$rotation, from=$from, to=$to]"

    fun loadInto(
      i: Int,
      faceCull: FaceCull,
      aoArray: AOArray,
      x: Int,
      y: Int,
      z: Int,
      builder: ModelBakery,
      renderInfo: RenderInfo,
      textureElements: Map<String, NamespaceID>,
      modelInfo: ModelInfo,
      jsonModel: JsonModel
    ) {
      val blockFaceFaceElementMap: Map<Direction, FaceElement> = this.blockFaceFaceElementMap
      val v00 = VertexInfo()
      val v01 = VertexInfo()
      val v10 = VertexInfo()
      val v11 = VertexInfo()
      for ((direction, faceElement) in blockFaceFaceElementMap.entries) {
        val dir1 = faceElement.cullfaceDir()
        if (dir1 != null && faceCull.face(dir1)) continue

        val ao = aoArray.aoForSide(direction)

        val texRef = faceElement.texture
        val texture: NamespaceID? = if (texRef == "#missing") NamespaceID.of(path = "textures/blocks/error.png")
        else if (texRef.startsWith("#")) textureElements[texRef.substring(1)]
        else NamespaceID.parse(texRef).mapPath { path -> "textures/$path.png" }

        v00.setCol(if (ao.hasAoCorner00) aoColor else Color.WHITE)
        v01.setCol(if (ao.hasAoCorner01) aoColor else Color.WHITE)
        v10.setCol(if (ao.hasAoCorner10) aoColor else Color.WHITE)
        v11.setCol(if (ao.hasAoCorner11) aoColor else Color.WHITE)

        v00.setNor(direction.normal)
        v01.setNor(direction.normal)
        v10.setNor(direction.normal)
        v11.setNor(direction.normal)

        val region = renderInfo.textureOffset.get(modelInfo, texture ?: continue, jsonModel) ?: continue

        val us = region.u2 - region.u
        val vs = region.v2 - region.v
        val x1 = region.u + faceElement.uvs.x1 * us
        val x2 = region.u + faceElement.uvs.x2 * us
        val y1 = region.v + faceElement.uvs.y1 * vs
        val y2 = region.v + faceElement.uvs.y2 * vs
        v00.setUV(x1, y2)
        v01.setUV(x1, y1)
        v10.setUV(x2, y2)
        v11.setUV(x2, y1)

        var dir: Byte = -1
        when (direction) {
          UP -> {
            v00.setPos(from.x, to.y, to.z)
            v01.setPos(from.x, to.y, from.z)
            v10.setPos(to.x, to.y, to.z)
            v11.setPos(to.x, to.y, from.z)
            dir = 0
          }

          DOWN -> {
            v00.setPos(from.x, from.y, from.z)
            v01.setPos(from.x, from.y, to.z)
            v10.setPos(to.x, from.y, from.z)
            v11.setPos(to.x, from.y, to.z)
            dir = 1
          }

          WEST -> {
            v00.setPos(from.x, from.y, from.z)
            v01.setPos(from.x, to.y, from.z)
            v10.setPos(from.x, from.y, to.z)
            v11.setPos(from.x, to.y, to.z)
            dir = 2
          }

          EAST -> {
            v00.setPos(to.x, from.y, to.z)
            v01.setPos(to.x, to.y, to.z)
            v10.setPos(to.x, from.y, from.z)
            v11.setPos(to.x, to.y, from.z)
            dir = 3
          }

          NORTH -> {
            v00.setPos(to.x, from.y, from.z)
            v01.setPos(to.x, to.y, from.z)
            v10.setPos(from.x, from.y, from.z)
            v11.setPos(from.x, to.y, from.z)
            dir = 4
          }

          SOUTH -> {
            v00.setPos(from.x, from.y, to.z)
            v01.setPos(from.x, to.y, to.z)
            v10.setPos(to.x, from.y, to.z)
            v11.setPos(to.x, to.y, to.z)
            dir = 5
          }
        }

        rotate(v00, v01, v10, v11, dir, rotation)

        v00.position.scl(1 / 16F).add(x.toFloat(), y.toFloat(), z.toFloat())
        v01.position.scl(1 / 16F).add(x.toFloat(), y.toFloat(), z.toFloat())
        v10.position.scl(1 / 16F).add(x.toFloat(), y.toFloat(), z.toFloat())
        v11.position.scl(1 / 16F).add(x.toFloat(), y.toFloat(), z.toFloat())

        builder.node(renderInfo, texture).rect(v00, v10, v11, v01)
      }
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is ModelElement) return false

      if (shade != other.shade) return false
      if (blockFaceFaceElementMap != other.blockFaceFaceElementMap) return false
      if (rotation != other.rotation) return false
      if (from != other.from) return false
      if (to != other.to) return false

      return true
    }

    override fun hashCode(): Int {
      var result = shade.hashCode()
      result = 31 * result + blockFaceFaceElementMap.hashCode()
      result = 31 * result + rotation.hashCode()
      result = 31 * result + from.hashCode()
      result = 31 * result + to.hashCode()
      return result
    }

    companion object {
      private val tmp = Vector3()
      private val tmpQ = Quaternion()
    }
  }

  class ElementRotation(
    val originVec: Vector3, val axis: Axis, val angle: Float,
    val rescale: Boolean,
  ) {

    fun originVec(): Vector3 = originVec

    fun axis(): Axis = axis

    fun angle(): Float = angle

    fun rescale(): Boolean = rescale


    override fun hashCode(): Int = Objects.hash(originVec, axis, angle, rescale)


    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is ElementRotation) return false

      if (angle != other.angle) return false
      if (rescale != other.rescale) return false
      if (originVec != other.originVec) return false
      if (axis != other.axis) return false

      return true
    }

    override fun toString(): String =
      "ElementRotation(originVec=$originVec, angle=$angle, rescale=$rescale, axis=$axis)"

    companion object {
      fun deserialize(rotation: JsonValue?): ElementRotation {
        if (rotation == null) {
          return ElementRotation(Vector3(0f, 0f, 0f), Axis.Y, 0f, false)
        }

        val origin: JsonValue =
          rotation["origin"].apply { if (!isArray) throw IOException("Invalid 'origin' array at ${rotation.trace()}: $rotation") }
        val axis: String = rotation["axis"].asString()
        val angle: Float = rotation["angle"].asFloat()
        val rescale: Boolean = rotation.getBoolean("rescale", false)

        val originVec = Vector3(origin[0].asFloat(), origin[1].asFloat(), origin[2].asFloat())
        return ElementRotation(originVec, Axis.valueOf(axis.uppercase()), angle, rescale)
      }
    }
  }

  class Display {
    override fun equals(other: Any?): Boolean = other === this || other != null && other.javaClass == this.javaClass

    override fun hashCode(): Int = 1

    override fun toString(): String = "Display[]"
  }
}
