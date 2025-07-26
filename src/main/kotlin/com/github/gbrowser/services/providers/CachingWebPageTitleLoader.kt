package com.github.gbrowser.services.providers

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.jsoup.Jsoup
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class CachingWebPageTitleLoader(
  private val scope: CoroutineScope
) : Disposable {

  companion object {
    private val LOG = logger<CachingWebPageTitleLoader>()
  }

  private val titleCache = Caffeine.newBuilder()
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build<String, CompletableFuture<String>>()

  /**
   * Gets the title of a web page asynchronously.
   * @return CompletableFuture for backward compatibility
   */
  fun getTitleOfWebPage(url: String): CompletableFuture<String> {
    return titleCache.get(url) { createCompletableFuture(url) }
  }

  private fun createCompletableFuture(url: String): CompletableFuture<String> {
    val future = CompletableFuture<String>()
    val deferred = loadTitleAsync(url)

    scope.launch {
      try {
        future.complete(deferred.await())
      } catch (e: CancellationException) {
        future.cancel(true)
      } catch (e: Exception) {
        future.completeExceptionally(e)
      }
    }

    return future
  }

  /**
   * Coroutine version for internal use and future migration
   */
  suspend fun getTitleOfWebPageSuspend(url: String): String {
    return titleCache.get(url) { createCompletableFuture(url) }.await()
  }

  private fun loadTitleAsync(url: String): Deferred<String> {
    return scope.async(Dispatchers.IO) {
      try {
        val document = Jsoup.connect(url).get()
        document.title().ifEmpty { "Unknown" }
      } catch (e: Exception) {
        LOG.info("Failed to load title of web page: $url", e)
        "Unknown"
      }
    }
  }

  override fun dispose() {
    titleCache.cleanUp()
    scope.cancel("Service disposed")
  }
}
