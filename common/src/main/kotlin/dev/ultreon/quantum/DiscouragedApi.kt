package dev.ultreon.quantum

/**
 * Marks an API as discouraged and should not be used by end users.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@RequiresOptIn(
  message = "It's not recommended to use this API.",
  level = RequiresOptIn.Level.WARNING
)
annotation class DiscouragedApi
