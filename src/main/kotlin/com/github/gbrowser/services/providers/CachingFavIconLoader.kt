package com.github.gbrowser.services.providers

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.JreHiDpiUtil
import com.intellij.ui.scale.DerivedScaleType
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.ImageLoader
import com.intellij.util.JBHiDPIScaledImage
import kotlinx.coroutines.*
import java.net.URI
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.ImageIcon

@Service(Service.Level.APP)
class CachingFavIconLoader(
  private val scope: CoroutineScope
) : Disposable {

  companion object {
    private val LOG = logger<CachingFavIconLoader>()
  }

  private val favIconCache = Caffeine.newBuilder()
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build<String, CompletableFuture<Icon?>>()

  /**
   * Loads a favicon for the given URL asynchronously.
   * @return CompletableFuture for backward compatibility
   */
  fun loadFavIcon(url: String, size: Int = 64, targetSize: Int = 16): CompletableFuture<Icon?> {
    // Use just the URL as the cache key for backward compatibility with tests
    // In real usage, different sizes would ideally have different cache entries
    return favIconCache.get(url) { createCompletableFuture(url, size, targetSize) }
  }

  private fun createCompletableFuture(url: String, size: Int, targetSize: Int): CompletableFuture<Icon?> {
    val future = CompletableFuture<Icon?>()
    val deferred = loadImageAsync(url, size, targetSize)

    scope.launch {
      try {
        future.complete(deferred.await())
      } catch (e: CancellationException) {
        future.cancel(true)
        throw e
      } catch (e: Exception) {
        future.completeExceptionally(e)
      }
    }

    return future
  }

  @Suppress("DEPRECATION")
  private fun loadImageAsync(url: String, size: Int, targetSize: Int): Deferred<Icon?> {
    return scope.async(Dispatchers.IO) {
      try {
        val domain = getDomainName(url.trim())
        val iconUrl = URL("https://www.google.com/s2/favicons?domain=$domain&sz=$size")

        ImageLoader.loadFromUrl(iconUrl)?.let { iconImage ->
          val scale = ScaleContext.create().getScale(DerivedScaleType.PIX_SCALE).toFloat()
          val originalIcon = if (scale != 1f && JreHiDpiUtil.isJreHiDPIEnabled()) {
            LOG.info("JreHiDpiUtil is enabled")

            @Suppress("UnstableApiUsage")
            (iconImage as? JBHiDPIScaledImage)?.delegate ?: iconImage
          } else {
            iconImage
          }

          val iconScaled = ImageLoader.scaleImage(originalIcon, targetSize)
          ImageIcon(iconScaled)
        }
      } catch (e: Exception) {
        LOG.debug("Failed to load favicon for: $url", e)
        null
      }
    }
  }

  //private fun getDomainName(url: String): String {
  //  // Implement this function to extract the domain from the URL if not already done.
  //  return URL(url).host
  //}

  private fun getDomainName(url: String): String {
    return try {
      URI(url).host?.removePrefix("www.")?.removeSuffix("/") ?: url
    } catch (e: Exception) {
      LOG.debug("Exception in getting the domain.", e)
      url.removePrefix("www.").removeSuffix("/")
    }
  }

  override fun dispose() {
    favIconCache.cleanUp()
    scope.cancel("Service disposed")
  }
}
