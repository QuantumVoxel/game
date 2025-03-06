package dev.ultreon.quantum.client

import dev.ultreon.quantum.client.scripting.ClientContextTypes
import dev.ultreon.quantum.event.Event
import dev.ultreon.quantum.event.EventRegistry
import dev.ultreon.quantum.scripting.ContextParam
import dev.ultreon.quantum.scripting.ContextType

/**
 * Event registry for client-side events
 */
val clientEvents = EventRegistry(quantum.clientResources).apply {
  register(
    Event(
      "game_tick",
      ContextParam("client", ClientContextTypes.client)
    )
  )

  register(
    Event(
      "resize",
      ContextParam("client", ClientContextTypes.client),
      ContextParam("width", ContextType.int),
      ContextParam("height", ContextType.int)
    )
  )

  register(
    Event(
      "show_screen",
      ContextParam("client", ClientContextTypes.client),
      ContextParam("old_screen", ClientContextTypes.screen),
      ContextParam("screen", ClientContextTypes.screen)
    )
  )
}
