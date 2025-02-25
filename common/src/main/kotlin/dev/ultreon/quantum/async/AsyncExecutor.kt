package dev.ultreon.quantum.async

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.async.AsyncTask

abstract class AsyncExecutor : Disposable {
  abstract fun <T> submit(task: () -> T): Future<T>
  abstract override fun dispose()
}
