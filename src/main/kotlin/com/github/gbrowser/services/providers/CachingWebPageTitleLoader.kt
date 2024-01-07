package com.github.gbrowser.services.providers

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import org.jsoup.Jsoup
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class CachingWebPageTitleLoader : Disposable {

  private val titleCache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES) // Adjust the time value as needed
    .build<String, CompletableFuture<String>>()

  fun getTitleOfWebPage(url: String): CompletableFuture<String> {
    return titleCache.get(url) { loadTitleAsync(url) }
  }

  private fun loadTitleAsync(url: String): CompletableFuture<String> {
    return CompletableFuture.supplyAsync {
      try {
        val document = Jsoup.connect(url).get()
        document.title()
      } catch (e: Exception) {
        "Unknown"
      }
    }
  }

  override fun dispose() {
    titleCache.cleanUp()
  }
}
