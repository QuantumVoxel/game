package dev.ultreon.quantum.world

import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.entity.Entity
import java.util.*

class EntityManager(val dimension: Dimension) : Disposable {
  val takenIds = BitSet()
  val entities = HashMap<Int, Entity>()

  fun createEntity(entity: Entity): Int {
    val id = entities.size
    entities[id] = entity
    takenIds.set(id)
    return id
  }

  fun removeEntity(id: Int) {
    entities.remove(id)
    takenIds.clear(id)
  }

  override fun dispose() {
    entities.clear()
    takenIds.clear()
  }
}
