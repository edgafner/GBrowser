package com.github.gbrowser.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest

fun CoroutineScope.cancelledWith(disposable: Disposable): CoroutineScope = apply {
  val job = coroutineContext[Job]
  requireNotNull(job) { "Coroutine scope without a parent job $this" }
  job.cancelOnDispose(disposable)
}

fun Job.cancelOnDispose(disposable: Disposable) {
  val childDisposable = Disposable { cancel("disposed") }
  Disposer.register(disposable, childDisposable)
  job.invokeOnCompletion {
    Disposer.dispose(childDisposable)
  }
}


@OptIn(ExperimentalCoroutinesApi::class)
fun <T, R> Flow<T>.mapScoped(mapper: CoroutineScope.(T) -> R): Flow<R> {
  return transformLatest { newValue ->
    coroutineScope {
      emit(mapper(newValue))
      awaitCancellation()
    }
  }
}
