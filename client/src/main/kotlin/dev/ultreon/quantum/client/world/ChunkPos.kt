package dev.ultreon.quantum.client.world

import com.badlogic.gdx.math.GridPoint3
import dev.ultreon.quantum.client.quantum

@JvmInline
value class ChunkPos(val point: GridPoint3) : Comparable<ChunkPos> {
  override fun compareTo(other: ChunkPos): Int {
    val chunkPosition = quantum.player?.positionComponent?.chunkPosition ?: return 0
    return point.dst(chunkPosition).compareTo(other.point.dst(quantum.player?.positionComponent?.chunkPosition))
  }
}
