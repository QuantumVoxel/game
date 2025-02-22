package dev.ultreon.quantum.client.world

import dev.ultreon.quantum.blocks.Block

object BlockRenderInfo {
  private val registry: MutableMap<Block, RenderInfo> = mutableMapOf()

  operator fun get(block: Block): RenderInfo {
    return registry[block] ?: RenderInfo.default
  }

  operator fun set(block: Block, renderInfo: RenderInfo) {
    registry[block] = renderInfo
  }
}
