package dev.ultreon.quantum.client.world

import dev.ultreon.quantum.client.LocalPlayerComponent
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.entity.*
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.network.Player
import dev.ultreon.quantum.world.Dimension

class LocalPlayer(override val name: String, val dimension: Dimension, position: Vector3D) : Player() {
  override lateinit var entity: Entity
  override val connection: Connection
    get() = quantum.connection ?: error("No connection")

  override fun messageReceived(message: String) {
    quantum.chat.addMessage(message)
  }

  fun createEntity() {
    EntityTemplate.player.createEntity(dimension).also {
      entity = it
    }
  }

  fun tick() {
    val chunkPosition = positionComponent.chunkPosition
    if (dimension.chunkAt(chunkPosition.x, chunkPosition.y, chunkPosition.z) == null) return
    physicsComponent.tick()
  }

  val positionComponent = PositionComponent(position)
  val playerComponent = LocalPlayerComponent()
  val runningComponent = RunningComponent()
  val inventoryComponent = InventoryComponent()
  val physicsComponent = PhysicsComponent()

  init {
    physicsComponent.positionComponent = positionComponent
    physicsComponent.dimension = dimension
  }
}
