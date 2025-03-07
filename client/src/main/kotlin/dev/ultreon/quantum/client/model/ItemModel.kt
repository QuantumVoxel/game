package dev.ultreon.quantum.client.model

import com.badlogic.gdx.graphics.g3d.Model

/**
 * Represents an item model.
 */
interface ItemModel {
  /**
   * Bakes the model.
   */
  fun bake(): Model
}
