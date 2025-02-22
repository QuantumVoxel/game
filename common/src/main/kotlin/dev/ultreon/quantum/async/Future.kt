package dev.ultreon.quantum.async

import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.gamePlatform
import ktx.assets.disposeSafely
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A little bit scuffed completable future replacement.
 * I mean, if it works, it works right?
 *
 * @author Qubilux
 * @since 0.0.2
 */
abstract class Future<T> protected constructor() {
  private val thread: Thread? = null
  private var cancelled: Boolean = false
  var value: T? = null
    private set
  var done: Boolean = false
    private set
  var throwable: Optional<Throwable> = Optional.empty()
    private set

  private var _onComplete: ((T) -> Unit)? = null
  @Suppress("UNCHECKED_CAST")
  open var onComplete: (T) -> Unit
    set(value) = synchronized(this) {
      if (done && throwable.isEmpty && _onComplete == null) value(this.value as T)
      _onComplete = value
    }
    get() = _onComplete ?: { }

  private var _onFailure: ((Throwable) -> Unit)? = null
  open var onFailure: (throwable: Throwable) -> Unit
    set(value) = synchronized(this) {
      if (done && throwable.isPresent && _onFailure == null) throw throwable.get()
      _onFailure = value
    }
    get() = _onFailure ?: { }

  private var _onDone: (() -> Unit)? = null
  open var onDone: () -> Unit
    set(value) = synchronized(this) {
      if (done && _onDone == null) value()
      _onDone = value
    }
    get() = _onDone ?: { }

  open fun complete(value: T) = synchronized(this) {
    if (done) return

    this.value = value
    this.done = true
    this.onComplete(value)
    this.onDone()
  }

  open fun completeExceptionally(throwable: Throwable) = synchronized(this) {
    if (done) return

    this.throwable = Optional.of(throwable)
    this.done = true
    this.onFailure(throwable)
    this.onDone()
  }

  open fun getNow(): Any? {
    return value
  }

  open fun isDone(): Boolean {
    return done
  }

  protected open fun waitUntil(timesOutAt: Long) {
    while (!done) {
      Thread.yield()

      if (System.currentTimeMillis() > timesOutAt) {
        throw TimeoutException()
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  open fun get(): T {
    while (!done) {
      Thread.yield()
    }
    return value as T
  }

  @Suppress("UNCHECKED_CAST")
  open fun get(timeoutMillis: Long): Any? {
    waitUntil(System.currentTimeMillis() + timeoutMillis)
    return value as T
  }

  @Suppress("UNCHECKED_CAST")
  open fun get(timeout: Long, unit: TimeUnit): Any? {
    waitUntil(System.currentTimeMillis() + unit.toMillis(timeout))
    return value as T
  }

  open fun isCancelled(): Boolean {
    return cancelled
  }

  open fun cancel() {
    synchronized(this) {
      if (cancelled) return
      cancelled = true
    }
  }

  companion object : Disposable {
    @Suppress("GDXKotlinStaticResource") // this is handled properly
    private val executor: AsyncExecutor = gamePlatform.createAsyncExecutor(gamePlatform.cpuCores())

    fun <T> completed(contextValue: T): Future<T> {
      val future = invoke<T>()
      future.complete(contextValue)
      return future
    }

    fun <T> failed(throwable: Throwable): Future<T> {
      val future = invoke<T>()
      future.completeExceptionally(throwable)
      return future
    }

    fun runAsync(function: () -> Unit): Future<Unit> {
      val future = invoke<Unit>()
      executor.submit {
        try {
          function()
          future.complete(Unit)
        } catch (e: Throwable) {
          future.completeExceptionally(e)
        }
      }

      return future
    }

    fun <T> supplyAsync(function: () -> T): Future<T> {
      val future = invoke<T>()
      executor.submit {
        try {
          future.complete(function())
        } catch (e: Throwable) {
          future.completeExceptionally(e)
        }
      }

      return future
    }

    override fun dispose() {
      executor.disposeSafely()
    }

    fun <T> supplyAsync(executor: AsyncExecutor, function: () -> T): Future<T> {
      val future = invoke<T>()
      executor.submit {
        try {
          future.complete(function())
        } catch (e: Throwable) {
          future.completeExceptionally(e)
        }
      }

      return future
    }

    fun <T> runAsync(executor: AsyncExecutor, function: () -> Unit): Future<Unit> {
      val future = invoke<Unit>()
      executor.submit {
        try {
          function()
          future.complete(Unit)
        } catch (e: Throwable) {
          future.completeExceptionally(e)
        }
      }

      return future
    }

    operator fun <T> invoke(): Future<T> {
      return gamePlatform.createFuture()
    }
  }
}
