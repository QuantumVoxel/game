package dev.ultreon.quantum.client.scripting

import dev.ultreon.quantum.client.gui.screens.IdScreen
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.scripting.ContextType
import dev.ultreon.quantum.scripting.ContextValue
import dev.ultreon.quantum.util.asIdOrNull

/**
 * Context types for client-side scripting contexts.
 * This object contains all the context types that are available on the client-side.
 *
 * @see ContextType
 * @see ContextValue
 */
object ClientContextTypes {
  val localPlayer = ContextType.register("local-player", parser = {
    return@register quantum.player?.let { ContextValue(this, it ) }
  })

  val screen = ContextType.register("screen", parser = {
    val id = it.get("id").asString().asIdOrNull() ?: run {
      logger.error("Invalid ID: ${it.asString()}")
      return@register null
    }
    val screen = IdScreen.get(id) ?: run {
      logger.error("Screen not found: $id")
      return@register null
    }

    return@register ContextValue(this, screen)
  })

  val batch = ContextType.register("batch", parser = {
    return@register ContextValue(this, quantum.globalBatch)
  })

  val font = ContextType.register("font", parser = {
    return@register ContextValue(this, quantum.font)
  })

  val guiRenderer = ContextType.register("gui-renderer", parser = {
    return@register ContextValue(this, quantum.guiRenderer)
  })

  val clientDimension = ContextType.register("client-dimension", parser = {
    return@register quantum.dimension?.let { ContextValue(this, it ) }
  })

  val client = ContextType.register("client", parser = {
    return@register ContextValue(this, quantum)
  })
}
