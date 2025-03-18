package dev.ultreon.quantum.server

import com.badlogic.gdx.math.GridPoint3
import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.scripting.PersistentData
import dev.ultreon.quantum.world.BlockFlags
import dev.ultreon.quantum.world.Chunk
import dev.ultreon.quantum.world.Dimension

/**
 * Represents a server dimension.
 */
@ExperimentalApi
class ServerDimension : Dimension() {
  /**
   * The chunks in the dimension.
   */
  val chunks = mutableMapOf<GridPoint3, ServerChunk>()

  /**
   * Gets the block at the specified coordinates.
   */
  override fun get(x: Int, y: Int, z: Int): Block {
    TODO("Not yet implemented")
  }

  /**
   * Sets the block at the specified coordinates.
   */
  override fun set(x: Int, y: Int, z: Int, block: Block, flags: BlockFlags) {
    TODO("Not yet implemented")
  }

  /**
   * Gets the chunk at the specified coordinates.
   */
  override fun chunkAt(x: Int, y: Int, z: Int): Chunk? {
    TODO("Not yet implemented")
  }

  /**
   * Persistent data for the dimension.
   */
  override val persistentData: PersistentData = PersistentData()
}
