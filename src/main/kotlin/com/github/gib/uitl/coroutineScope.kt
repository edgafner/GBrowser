package com.github.gib.uitl

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun requireNoJob(context: CoroutineContext) {
  require(context[Job] == null) {
    "Context must not specify a Job: $context"
  }
}


fun CoroutineScope.childScope(context: CoroutineContext = EmptyCoroutineContext, supervisor: Boolean = true): CoroutineScope {
  requireNoJob(context)
  return ChildScope(coroutineContext + context, supervisor)
}

fun CoroutineScope.namedChildScope(
  name: String,
  context: CoroutineContext = EmptyCoroutineContext,
  supervisor: Boolean = true,
): CoroutineScope {
  requireNoJob(context)
  return ChildScope(coroutineContext + context + CoroutineName(name), supervisor)
}

/**
 * This allows to see actual coroutine context (and name!)
 * in the coroutine dump instead of "SupervisorJobImpl{Active}@598294b2".
 *
 * [See issue](https://github.com/Kotlin/kotlinx.coroutines/issues/3428)
 */
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
private class ChildScope(ctx: CoroutineContext, private val supervisor: Boolean) : JobImpl(ctx[Job]), CoroutineScope {

  override fun childCancelled(cause: Throwable): Boolean {
    return !supervisor && super.childCancelled(cause)
  }

  override val coroutineContext: CoroutineContext = ctx + this

  override fun toString(): String {
    val coroutineName = coroutineContext[CoroutineName]?.name
    return (if (coroutineName != null) "\"$coroutineName\":" else "") +
           (if (supervisor) "supervisor:" else "") +
           super.toString()
  }
}


fun Job.cancelOnDispose(disposable: Disposable) {
  val childDisposable = Disposable { cancel("disposed") }
  Disposer.register(disposable, childDisposable)
  job.invokeOnCompletion {
    Disposer.dispose(childDisposable)
  }
}

@OptIn(InternalCoroutinesApi::class)
fun CoroutineScope.nestedDisposable(): Disposable {
  val job = coroutineContext[Job]
  require(job != null) {
    "Found no Job in context: $coroutineContext"
  }
  return Disposer.newDisposable().also {
    job.invokeOnCompletion(onCancelling = true, handler = { _ ->
      Disposer.dispose(it)
    })
  }
}

fun CoroutineScope.cancelledWith(disposable: Disposable): CoroutineScope = apply {
  val job = coroutineContext[Job]
  requireNotNull(job) { "Coroutine scope without a parent job $this" }
  job.cancelOnDispose(disposable)
}