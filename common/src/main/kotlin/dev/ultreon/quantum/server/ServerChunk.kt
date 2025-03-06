@file:OptIn(ExperimentalApi::class)

package dev.ultreon.quantum.server

import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.blocks.Block
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.math.Vector3D
import dev.ultreon.quantum.world.BlockFlags
import dev.ultreon.quantum.world.Chunk
import dev.ultreon.quantum.world.SIZE

class ServerChunk(override val offset: Vector3D) : Chunk() {
  val data = PaletteStorage(SIZE * SIZE * SIZE, Blocks.air)
  var disposed = false

  override fun get(x: Int, y: Int, z: Int): Block {
    return data[Index3(x, y, z).index]
  }

  override fun set(x: Int, y: Int, z: Int, block: Block) {
    data[Index3(x, y, z).index] = block
  }

  override fun set(x: Int, y: Int, z: Int, block: Block, flags: BlockFlags) {
    data[Index3(x, y, z).index] = block
  }

  override fun isDisposed(): Boolean {
    return disposed
  }

  override fun dispose() {
    disposed = true
  }
}
