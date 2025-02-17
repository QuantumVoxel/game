package dev.ultreon.quantum.lwjgl3

import dev.ultreon.quantum.async.Future
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Lwjgl3Future<T> : Future<T>() {
  private val completableFuture = CompletableFuture<T>()

  private var _onCompleted: ((T) -> Unit)? = null

  override var onComplete: (T) -> Unit
    get() = _onCompleted ?: { }
    set(value) {
      _onCompleted = value

      completableFuture.thenAccept {
        value(it)
      }

      if (completableFuture.isDone) {
        value(completableFuture.get())
      }
    }

  private var _onDone: (() -> Unit)? = null

  override var onDone: () -> Unit
    get() = _onDone ?: { }
    set(value) {
      _onDone = value
      if (completableFuture.isDone) {
        value()
      }

      completableFuture.whenComplete { _, _ ->
        value()
      }
    }

  private var _onFailure: ((throwable: Throwable) -> Unit)? = null

  override var onFailure: (throwable: Throwable) -> Unit
    get() = _onFailure ?: { }
    set(value) {
      _onFailure = value
      completableFuture.exceptionally {
        value(it)
        return@exceptionally null
      }
    }

  override fun getNow(): Any? {
    return completableFuture.getNow(null)
  }

  override fun complete(value: T) {
    completableFuture.complete(value)
  }

  override fun completeExceptionally(throwable: Throwable) {
    completableFuture.completeExceptionally(throwable)
  }

  override fun get(): T {
    return completableFuture.get()
  }

  override fun get(timeoutMillis: Long): T {
    return completableFuture.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
  }

  override fun get(timeout: Long, unit: TimeUnit): T {
    return completableFuture.get(timeout, unit)
  }

  override fun isDone(): Boolean {
    return completableFuture.isDone
  }

  override fun waitUntil(timesOutAt: Long) {
    completableFuture.get(timesOutAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
  }

  override fun cancel() {
    completableFuture.cancel(true)
  }

  override fun isCancelled(): Boolean {
    return completableFuture.isCancelled
  }
}
