package dev.ultreon.quantum

/**
 * Used to mark an API as internal and should not be used by end users.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@RequiresOptIn(
  level = RequiresOptIn.Level.ERROR,
  message = "This API is internal and should not be used by end users"
)

annotation class InternalApi
