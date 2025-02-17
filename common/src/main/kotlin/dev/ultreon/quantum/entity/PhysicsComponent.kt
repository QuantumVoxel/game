package dev.ultreon.quantum.entity

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.math.BoundingBoxD
import dev.ultreon.quantum.scripting.`null`
import dev.ultreon.quantum.scripting.load
import dev.ultreon.quantum.util.BoundingBoxUtils
import dev.ultreon.quantum.util.Tickable
import dev.ultreon.quantum.vec3d
import dev.ultreon.quantum.world.Dimension

private val tmp1 = vec3d()
private val tmp2 = vec3d()

class PhysicsComponent : Component<PhysicsComponent>(), Tickable {
  lateinit var positionComponent: PositionComponent
  lateinit var dimension: Dimension
  private var oDx: Double = 0.0
  private var oDy: Double = 0.0
  private var oDz: Double = 0.0
  private var fallDistance: Double = 0.0
  var velocityX: Double = 0.0
  var velocityY: Double = 0.0
  var velocityZ: Double = 0.0
  var onGround: Boolean = false
  var wasOnGround: Boolean = false
  var isColliding: Boolean = false
    private set
  var isCollidingX: Boolean = false
    private set
  var isCollidingY: Boolean = false
    private set
  var isCollidingZ: Boolean = false
    private set
  val boundingBox: BoundingBoxD
    get() = BoundingBoxD(
      tmp1.set(positionComponent.position).sub(0.3, 0.0, 0.3),
      tmp2.set(positionComponent.position).add(0.3, 1.8, 0.3)
    )
  var noClip: Boolean = false
  var x: Double
    get() = positionComponent.position.x
    set(v) {
      positionComponent.position.x = v
    }
  var y: Double
    get() = positionComponent.position.y
    set(v) {
      positionComponent.position.y = v
    }
  var z: Double
    get() = positionComponent.position.z
    set(v) {
      positionComponent.position.z = v
    }

  var onMoved = {}

  /**
   * Moves the entity by the specified deltas.
   *
   * @param deltaX the change in x-coordinate
   * @param deltaY the change in y-coordinate
   * @param deltaZ the change in z-coordinate
   * @return true if the entity is colliding after the move, false otherwise
   */
  private fun move(deltaX: Double, deltaY: Double, deltaZ: Double): Boolean {
    // Trigger an event to allow modification of the move
    // TODO: Event
//    val event: EntityMoveEvent = EntityMoveEvent(this, Vec(deltaX, deltaY, deltaZ))
//    ModApi.getGlobalEventHandler().call(event)
//    val modifiedValue: Vec = event.getDelta()
//
//    if (event.isCanceled()) {
//      return this.isColliding
//    }
//
//    // If the event is canceled and a modified value is provided, update the deltas
//    deltaX = modifiedValue.x
//    deltaY = modifiedValue.y
//    deltaZ = modifiedValue.z

    // Store the original deltas after potential modification
    val originalDeltaXModified = deltaX
    val originalDeltaYModified = deltaY
    val originalDeltaZModified = deltaZ

    // Update the bounding box based on the modified deltas
    val updatedBoundingBoxD: BoundingBoxD = this.boundingBox.updateByDelta(deltaX, deltaY, deltaZ)

    // Move the entity based on the updated bounding box and deltas
    if (this.noClip) {
      this.x += deltaX
      this.y += deltaY
      this.z += deltaZ
      this.onMoved()
      // TODO: Networking
//      this.pipeline.putDouble("x", this.x)
//      this.pipeline.putDouble("y", this.y)
//      this.pipeline.putDouble("z", this.z)
    } else {
      this.moveWithCollision(
        updatedBoundingBoxD,
        deltaX,
        deltaY,
        deltaZ,
        originalDeltaXModified,
        originalDeltaYModified,
        originalDeltaZModified
      )
      this.onMoved()
      // TODO: Networking
//      this.pipeline.putDouble("x", this.x)
//      this.pipeline.putDouble("y", this.y)
//      this.pipeline.putDouble("z", this.z)
    }

    return this.isColliding
  }

  /**
   * Moves the entity with collision detection and response.
   *
   * @param ext Bounding box of the entity
   * @param dx Change in x-coordinate
   * @param dy Change in y-coordinate
   * @param dz Change in z-coordinate
   * @param oldDx Original change in x-coordinate
   * @param oldDy Original change in y-coordinate
   * @param oldDz Original change in z-coordinate
   */
  private fun moveWithCollision(
    ext: BoundingBoxD,
    dx: Double,
    dy: Double,
    dz: Double,
    oldDx: Double,
    oldDy: Double,
    oldDz: Double,
  ) {
    // Get list of bounding boxes the entity collides with
    var dex = dx
    var dey = dy
    var dez = dz
    val boxes: List<BoundingBoxD> = this.dimension.collide(ext, false)

    val pBox: BoundingBoxD = this.boundingBox // Get the entity's bounding box

    this.isColliding = false
    this.isCollidingY = false

    // Check collision and update y-coordinate
    for (box in boxes) {
      val dy2: Double = BoundingBoxUtils.clipYCollide(box, pBox, dey)
      this.isColliding = this.isColliding or (dey != dy2)
      this.isCollidingY = this.isCollidingY or (dey != dy2)
      dey = dy2
    }

    // Update the y-coordinate of the bounding box
    pBox.min.add(0.0, dey, 0.0)
    pBox.max.add(0.0, dey, 0.0)
    pBox.update()

    this.isCollidingX = false

    // Check collision and update x-coordinate
    for (box in boxes) {
      val dx2: Double = BoundingBoxUtils.clipXCollide(box, pBox, dex)
      this.isColliding = this.isColliding or (dex != dx2)
      this.isCollidingX = this.isCollidingX or (dex != dx2)
      dex = dx2
    }

    // Update the x-coordinate of the bounding box
    pBox.min.add(dex, 0.0, 0.0)
    pBox.max.add(dex, 0.0, 0.0)
    pBox.update()

    this.isCollidingZ = false

    // Check collision and update z-coordinate
    for (box in boxes) {
      val dz2: Double = BoundingBoxUtils.clipZCollide(box, pBox, dez)
      this.isColliding = this.isColliding or (dez != dz2)
      this.isCollidingZ = this.isCollidingZ or (dez != dz2)
      dez = dz2
    }

    // Update the z-coordinate of the bounding box
    pBox.min.add(0.0, 0.0, dez)
    pBox.max.add(0.0, 0.0, dez)
    pBox.update()

    // Check if entity is on the ground
    this.wasOnGround = this.onGround
    this.onGround = oldDy != dey && oldDy < 0.0f

    // Reset velocity if there was a collision in x-coordinate
    if (oldDx != dex) {
      this.velocityX = 0.0
    }

    // Reset fall distance if entity is moving upwards
    if (dey >= 0) {
      this.fallDistance = 0.0
    }

    // Handle collision responses and update fall distance
    if (this.onGround && !this.wasOnGround) {
      this.hitGround()
      this.fallDistance = 0.0
      this.velocityY = 0.0
      dey = 0.0
    } else if (dey < 0) {
      this.fallDistance -= dey
    }

    // Reset velocity if there was a collision in z-coordinate
    if (oldDz != dez) {
      this.velocityZ = 0.0
    }

    // Update entity's position
    this.x = (pBox.min.x + pBox.max.x) / 2.0f
    this.y = pBox.min.y
    this.z = (pBox.min.z + pBox.max.z) / 2.0f

    this.oDx = dex
    this.oDy = dey
    this.oDz = dez
  }

  private fun hitGround() {
    this.fallDistance = 0.0
  }

  fun move() {
    if (!this.onGround) velocityY -= 0.06
    velocityX *= 0.6
    velocityZ *= 0.6
    move(velocityX, velocityY, velocityZ)
  }

  override fun tick() {
    move()
  }

  override val componentType = ComponentType.physics
  override fun json(): JsonValue {
    return JsonValue(JsonValue.ValueType.`object`).also { json ->
      json.addChild("velocityX", JsonValue(velocityX))
      json.addChild("velocityY", JsonValue(velocityY))
      json.addChild("velocityZ", JsonValue(velocityZ))
      json.addChild("fallDistance", JsonValue(fallDistance))
      json.addChild("isColliding", JsonValue(isColliding))
      json.addChild("isCollidingX", JsonValue(isCollidingX))
      json.addChild("isCollidingY", JsonValue(isCollidingY))
      json.addChild("isCollidingZ", JsonValue(isCollidingZ))
      json.addChild("wasOnGround", JsonValue(wasOnGround))
      json.addChild("onGround", JsonValue(onGround))
      json.addChild("noClip", JsonValue(noClip))
      json.addChild("boundingBox", boundingBox.`null`())
    }
  }

  override fun load(json: JsonValue) {
    velocityX = json["velocityX"]?.asDouble() ?: 0.0
    velocityY = json["velocityY"]?.asDouble() ?: 0.0
    velocityZ = json["velocityZ"]?.asDouble() ?: 0.0
    fallDistance = json["fallDistance"]?.asDouble() ?: 0.0
    isColliding = json["isColliding"]?.asBoolean() ?: false
    isCollidingX = json["isCollidingX"]?.asBoolean() ?: false
    isCollidingY = json["isCollidingY"]?.asBoolean() ?: false
    isCollidingZ = json["isCollidingZ"]?.asBoolean() ?: false
    wasOnGround = json["wasOnGround"]?.asBoolean() ?: false
    onGround = json["onGround"]?.asBoolean() ?: false
    noClip = json["noClip"]?.asBoolean() ?: false
    boundingBox.load(json["boundingBox"])
  }
}

private fun BoundingBoxD.updateByDelta(deltaX: Double, deltaY: Double, deltaZ: Double): BoundingBoxD {
  this.min.add(deltaX, deltaY, deltaZ)
  this.max.add(deltaX, deltaY, deltaZ)
  return this
}
