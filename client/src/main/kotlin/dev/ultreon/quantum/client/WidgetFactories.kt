package dev.ultreon.quantum.client

import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.client.gui.widget.Widget
import kotlin.reflect.KClass

/**
 * A function that creates a new instance of a widget.
 *
 * @see WidgetFactories
 */
fun interface WidgetFactory<T> {
  fun create(parent: GuiContainer?): T
}

/**
 * Widget factories are a way to add custom widgets to the game. They allow you to create widgets with
 * unique behaviors and appearances without changing the core codebase.
 *
 * Modders can create their own widget implementations and register them with the [WidgetFactories]
 * object. This makes it easy to add new features and customization options without having to modify
 * the core codebase.
 *
 * ### Example usage:
 * ```kotlin
 * class CustomWidget : Widget() {
 *     // Widget-specific implementation
 * }
 *
 * val customWidgetFactory = WidgetFactory<CustomWidget> {
 *     CustomWidget()
 * }
 *
 * // Register the custom widget factory
 * WidgetFactories.register(customWidgetFactory)
 * ```
 *
 */
object WidgetFactories {
  private val factories = mutableMapOf< KClass<out Widget>, WidgetFactory<out Widget>>()

  /**
   * Registers a new widget factory using the specified [clazz] and [factory].
   *
   * @param clazz The class of the widget to register.
   * @param factory The factory to register.
   */
  fun register(clazz: KClass<out Widget>, factory: WidgetFactory<out Widget>) {
    factories[clazz] = factory
  }

  /**
   * Registers a new widget factory using the specified [clazz] and [factory].
   *
   * @param factory The factory to register.
   * @param T The class of the widget to register.
   */
  inline fun <reified T : Widget> register(factory: WidgetFactory<T>) {
    register(T::class, factory)
  }

  /**
   * Unregisters a widget factory using the specified [clazz].
   *
   * @param clazz The class of the widget to unregister.
   */
  fun unregister(clazz: KClass<out Widget>) {
    factories.remove(clazz)
  }

  /**
   * Unregisters a widget factory using the specified [clazz].
   *
   * @param T The class of the widget to unregister.
   */
  inline fun <reified T : Widget> unregister() {
    unregister(T::class)
  }

  /**
   * Creates a new instance of a widget using the specified [clazz].
   *
   * @param clazz The class of the widget to create.
   * @return A new instance of the widget.
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Widget> create(clazz: KClass<out T>, parent: GuiContainer?): T {
    return (factories[clazz]?.create(parent) ?: error("No factory found for class $clazz")) as T
  }

  /**
   * Creates a new instance of a widget using the specified [clazz].
   *
   * @param T The class of the widget to create.
   * @return A new instance of the widget.
   */
  inline fun <reified T : Widget> create(parent: GuiContainer?): T {
    return create(T::class, parent)
  }
}
