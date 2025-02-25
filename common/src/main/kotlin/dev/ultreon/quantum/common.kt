package dev.ultreon.quantum

import com.badlogic.gdx.utils.Json
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.event.commonEvents
//import dev.ultreon.quantum.event.EventBus
import dev.ultreon.quantum.item.Item
import dev.ultreon.quantum.item.Items
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.registry.Registries
import dev.ultreon.quantum.resource.ResourceId
import dev.ultreon.quantum.resource.ResourceManager
import dev.ultreon.quantum.util.NamespaceID

val logger = LoggerFactory["QuantumVoxel"]
val json: Json = Json()

val Block.id: NamespaceID
  get() = Registries.blocks[this] ?: throw NoSuchElementException("Block not registered: $this")

val Item.id: NamespaceID
  get() = Registries.items[this] ?: throw NoSuchElementException("Item not registered: $this")

val Block.key: ResourceId<Block>
  get() = Registries.blocks.getKey(this) ?: throw NoSuchElementException("Block not registered: $this")

val Item.key: ResourceId<Item>
  get() = Registries.items.getKey(this) ?: throw NoSuchElementException("Item not registered: $this")

fun vec3d(x: Double, y: Double, z: Double): Vector3D {
  return Vector3D(x, y, z)
}

fun vec3d(x: Int, y: Int, z: Int): Vector3D {
  return Vector3D(x.toDouble(), y.toDouble(), z.toDouble())
}

fun vec3d() = vec3d(0.0, 0.0, 0.0)

fun vec3d(x: Float, y: Float, z: Float): Vector3D {
  return Vector3D(x.toDouble(), y.toDouble(), z.toDouble())
}

val commonResources = ResourceManager("common")

fun doContentRegistration() {
  Blocks.loadContent(commonResources)
  Items.loadContent(commonResources)

  commonEvents.load()
}

//val commonEventBus = EventBus()
