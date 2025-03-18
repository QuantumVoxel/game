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

/**
 * The main logger for QuantumVoxel.
 */
val logger = LoggerFactory["QuantumVoxel"]

/**
 * An instance of the [Json] class for parsing and serializing JSON.
 */
val json: Json = Json()

/**
 * Returns the [NamespaceID] of the given [Block].
 */
val Block.id: NamespaceID
  get() = Registries.blocks[this] ?: throw NoSuchElementException("Block not registered: $this")

/**
 * Returns the [NamespaceID] of the given [Item].
 */
val Item.id: NamespaceID
  get() = Registries.items[this] ?: throw NoSuchElementException("Item not registered: $this")

/**
 * Returns the [ResourceId] of the given [Block].
 */
val Block.key: ResourceId<Block>
  get() = Registries.blocks.getKey(this) ?: throw NoSuchElementException("Block not registered: $this")

/**
 * Returns the [ResourceId] of the given [Item].
 */
val Item.key: ResourceId<Item>
  get() = Registries.items.getKey(this) ?: throw NoSuchElementException("Item not registered: $this")

/**
 * Creates a new [Vector3D] with the given [x], [y], and [z].
 */
fun vec3d(x: Double, y: Double, z: Double): Vector3D {
  return Vector3D(x, y, z)
}

/**
 * Creates a new [Vector3D] with the given [x], [y], and [z].
 */
fun vec3d(x: Int, y: Int, z: Int): Vector3D {
  return Vector3D(x.toDouble(), y.toDouble(), z.toDouble())
}

/**
 * Creates a new [Vector3D] with only zeros.
 */
fun vec3d() = vec3d(0.0, 0.0, 0.0)

/**
 * Creates a new [Vector3D] with the given [x], [y], and [z].
 */
fun vec3d(x: Float, y: Float, z: Float): Vector3D {
  return Vector3D(x.toDouble(), y.toDouble(), z.toDouble())
}

/**
 * The common resources for the Quantum Voxel Engine.
 */
val commonResources = ResourceManager("common")

/**
 * Registers all content for the common module.
 */
fun doContentRegistration() {
  Blocks.loadContent(commonResources)
  Items.loadContent(commonResources)

  commonEvents.load()
}

//val commonEventBus = EventBus()
