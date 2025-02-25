package dev.ultreon.quantum.async

import com.badlogic.gdx.utils.GdxRuntimeException
import java.util.concurrent.ExecutionException

class AsyncResult<T>(private val future: Future<T>) {
  val done: Boolean
    get() = future.done

  fun get(): T? {
    return try {
      future.get()
    } catch (e: InterruptedException) {
      null
    } catch (e: ExecutionException) {
      throw RuntimeException(e.cause)
    }
  }
}
