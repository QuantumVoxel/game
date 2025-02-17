package dev.ultreon.quantum.lwjgl3

import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.AsyncResult
import dev.ultreon.quantum.async.Future

class Lwjgl3AsyncExecutor(maxConcurrent: Int, name: String) : AsyncExecutor() {
  private val gdx = com.badlogic.gdx.utils.async.AsyncExecutor(maxConcurrent, name)

  override fun <T> submit(task: () -> T): Future<T> {
    val future = Lwjgl3Future<T>()

    gdx.submit {
      try {
        future.complete(task())
      } catch (e: Throwable) {
        future.completeExceptionally(e)
      }
    }

    return future
  }

  override fun dispose() {
    gdx.dispose()
  }
}
