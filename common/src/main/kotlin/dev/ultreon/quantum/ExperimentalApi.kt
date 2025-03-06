package dev.ultreon.quantum

/**
 * Marks an API as experimental and may change in the future.
 *
 * This annotation is used to mark APIs that are experimental and may change in the future.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@RequiresOptIn(
  message = "This API is experimental and may change in the future",
  level = RequiresOptIn.Level.WARNING
)
annotation class ExperimentalApi
