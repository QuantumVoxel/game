@file:JvmName("BlocksKt")

package dev.ultreon.quantum.blocks

import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.scripting.ContextAware
import dev.ultreon.quantum.id
import dev.ultreon.quantum.math.BoundingBoxD
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.registry.Registries
import dev.ultreon.quantum.registry.Registry
import dev.ultreon.quantum.resource.ResourceManager
import dev.ultreon.quantum.resource.asDirOrNull
import dev.ultreon.quantum.resource.asDirectoryOrNull
import dev.ultreon.quantum.scripting.ContextType
import dev.ultreon.quantum.scripting.PersistentData
import dev.ultreon.quantum.util.NamespaceID
import dev.ultreon.quantum.util.asIdOrNull
import dev.ultreon.quantum.util.id
import dev.ultreon.quantum.vec3d
import dev.ultreon.quantum.world.BlockElement
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.collections.toGdxArray
import ktx.math.vec3
import kotlin.reflect.KProperty

class Block : ContextAware<Block> {
  val element: BlockElement = BlockElement(this)
  val definition: BlockStateDefinition = BlockStateDefinition(GdxArray())
  val isOpaque: Boolean
    get() = renderType == "default"
  val isAir: Boolean
    get() = this == Blocks.air
  var isFluid: Boolean = false
  var renderType: String = "default"
  var ambientOcclusion: Boolean = !isFluid && !isAir
  var hasCollider: Boolean = true
  val bounds: GdxArray<BoundingBox> = gdxArrayOf(
    BoundingBox(
      vec3(0f, 0f, 0f),
      vec3(1f, 1f, 1f)
    )
  )

  val isSolid: Boolean = true

  override val persistentData: PersistentData = PersistentData()

  override fun contextType(): ContextType<Block> {
    return ContextType.block
  }

  override fun toString(): String {
    return "Block($id)"
  }

  fun boundsAt(vec3d: Vector3D): GdxArray<BoundingBoxD> {
    return bounds.map {
      BoundingBoxD(
        Vector3D(it.min.x + vec3d.x, it.min.y + vec3d.y, it.min.z + vec3d.z),
        Vector3D(it.max.x + vec3d.x, it.max.y + vec3d.y, it.max.z + vec3d.z)
      )
    }.toGdxArray()
  }

  fun boundsAt(x: Double, y: Double, z: Double): GdxArray<BoundingBoxD> {
    return boundsAt(vec3d(x, y, z))
  }

  fun boundsAt(x: Int, y: Int, z: Int): GdxArray<BoundingBoxD> {
    return boundsAt(vec3d(x.toDouble(), y.toDouble(), z.toDouble()))
  }

  companion object {
    fun of(json: JsonValue): Block {
      val parseBlockState = parseBlockState(json)
      val collision = parseBlockState.physics.collision

      val block = Block()
      block.hasCollider = collision == BlockPhysics.Collision.SOLID
      block.isFluid = collision == BlockPhysics.Collision.LIQUID
      block.renderType = parseBlockState.rendering.renderType
      block.ambientOcclusion = parseBlockState.rendering.ambientOcclusion

      return block
    }
  }
}

fun block(func: Block.() -> Unit): Block {
  val block = Block()
  block.func()
  return block
}

object Blocks : GameContent<Block>(Registries.blocks) {
  /*

  val air = register("air", Block().apply { hasCollider = false })
  val soil = register("soil", Block())
  val grass = register("grass", Block())
  val stone = register("stone", Block())
  val crate = register("crate", Block())
  val water = register("water", Block().apply { renderType = "water"; isFluid = true })
  val sand = register("sand", Block())
  val cobblestone = register("cobblestone", Block())
  val snowyGrass = register("snowy_grass", Block())
  val shortGrass = register("short_grass", Block().apply { renderType = "foliage"; hasCollider = false })
  val iron = register("iron_block", Block())

   */
  val air = register("air", Block().apply { hasCollider = false; ambientOcclusion = false })
  val soil by key(Registries.blocks, NamespaceID.of(path = "soil"))
  val grass by key(Registries.blocks, id(path = "grass_block"))
  val stone by key(Registries.blocks, id(path = "stone"))
  val water by key(Registries.blocks, id(path = "water"))
  val sand by optionalKey(Registries.blocks, id(path = "sand"))
  val crate by optionalKey(Registries.blocks, id(path = "crate"))
  val cobblestone by optionalKey(Registries.blocks, id(path = "cobblestone"))
  val snowyGrass by optionalKey(Registries.blocks, id(path = "snowy_grass"))
  val sandstone by optionalKey(Registries.blocks, id(path = "sandstone"))
  val snowBlock by optionalKey(Registries.blocks, id(path = "snow_block"))
  val shortGrass by optionalKey(Registries.blocks, id(path = "short_grass"))
  val iron by optionalKey(Registries.blocks, id(path = "iron_block"))

  override fun loadContent(resources: ResourceManager) {
    resources["blocks"]?.asDirOrNull()?.get("states")?.asDirOrNull()?.asDirectoryOrNull()?.walk {
      it.location.path.split('/').let { path ->
        val drop = path.drop(2)
        if (!drop.last().endsWith(".quant")) return@let
        NamespaceID.of(it.location.domain, drop.joinToString("/").substringBeforeLast(".")).let { id ->
          val of = Block.of(it.json())
          Registries.blocks.register(id, of)
        }
      }
    }
    resources["blocks"]?.asDirOrNull()?.get("statedefs")?.asDirOrNull()?.asDirectoryOrNull()?.walk {
      // TODO
    }
  }

  operator fun get(id: NamespaceID): Block? {
    return Registries.blocks[id]
  }

  fun get(name: String): Block? {
    return Registries.blocks[name.asIdOrNull() ?: return null]
  }
}

class KeyDelegate<T : Any>(val registry: Registry<T>, val of: NamespaceID) {
  operator fun getValue(thisRef: Any, property: KProperty<*>): T {
    return registry[of] ?: throw IllegalStateException("Block not registered: $of")
  }
}

fun <T : Any> key(blocks: Registry<T>, of: NamespaceID): KeyDelegate<T> {
  return KeyDelegate(blocks, of)
}

class OptionalKeyDelegate<T : Any>(val registry: Registry<T>, val of: NamespaceID) {
  operator fun getValue(thisRef: Any, property: KProperty<*>): T? = registry[of]
}

fun <T : Any> optionalKey(blocks: Registry<T>, of: NamespaceID): OptionalKeyDelegate<T> {
  return OptionalKeyDelegate(blocks, of)
}
