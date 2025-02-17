package dev.ultreon.quantum.event

import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.commonResources
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.resource.*
import dev.ultreon.quantum.scripting.ContextParam
import dev.ultreon.quantum.scripting.ContextValue
import dev.ultreon.quantum.scripting.function.CallContext
import dev.ultreon.quantum.scripting.function.VirtualFunction
import dev.ultreon.quantum.scripting.function.VirtualFunctions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class Event(val name: String, vararg val contextParams: ContextParam<*>) {
  private val resourceList = mutableListOf<Resource>()
  private val listeners = mutableListOf<VirtualFunction>()

  fun addResource(resource: Resource) {
    resourceList.add(resource)
  }

  fun clearResources() {
    resourceList.clear()
  }

  fun load() {
    for (resource in resourceList) run loop@ {
      val json = resource.json()
      if (!json.isObject) {
        logger.error("JSON is not an object in event: $name")
        return@loop
      }
      val listenersArray = json["listeners"] ?: run {
        logger.error("Listeners not found in event: $name")
        return@loop
      }
      if (!listenersArray.isArray) {
        logger.error("Listeners is not an array in event: $name")
        return@loop
      }
      loadListeners(listenersArray)
    }
  }

  fun clear() {
    resourceList.clear()
    listeners.clear()
  }

  private fun loadListeners(json: JsonValue) {
    for (element in json) {
      if (!element.isObject) continue
      VirtualFunctions[element.get("type")?.asString() ?: run {
        logger.error("Event type not found: ($name.quant).${element.trace()}")
        return
      }]?.let { this.listeners += it(element) }
    }
  }

  fun callAsync(vararg mapArgs: Pair<String, ContextValue<*>>) {
    var start = System.currentTimeMillis()
    for (listener in listeners) {
      listener.call(CallContext(listener.contextJson).also {
        it.paramValues.putAll(mapArgs)
      }).get()

      if ((System.currentTimeMillis() - start) > 50) {
        start = System.currentTimeMillis()
      }
    }
  }

  fun callSync(vararg mapArgs: Pair<String, ContextValue<*>>) {
    callAsync(*mapArgs)
  }
}

class EventRegistry(private val resourceManager: ResourceManager) {
  private val events = mutableMapOf<String, Event>()

  fun register(event: Event) {
    events[event.name] = event
  }

  fun unregister(event: Event) {
    events.remove(event.name)
  }

  operator fun get(name: String) = events[name]

  fun clear() {
    events.clear()
  }

  fun load() {
    for (event in events.values) {
      val eventsDir = resourceManager["events"]
      if (eventsDir != null && eventsDir.asDirOrNull() != null) {
        loadResource(eventsDir, event)
      }

      event.load()
    }
  }

  private fun loadResource(eventsDir: ResourceNode, event: Event) {
    val eventResource = eventsDir.asDir()["${event.name}.quant"]
    if (eventResource != null && eventResource.asLeafOrNull() != null) {
      eventResource.asLeaf().forEach {
        event.addResource(it)
      }
    }
  }
}

val commonEvents = EventRegistry(commonResources)
