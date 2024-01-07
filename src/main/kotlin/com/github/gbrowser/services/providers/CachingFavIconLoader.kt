package com.github.gbrowser.services.providers

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.util.ImageLoader
import java.net.URI
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.ImageIcon

@Service(Service.Level.APP)
class CachingFavIconLoader : Disposable {

  private val favIconCache = Caffeine.newBuilder()
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build<String, CompletableFuture<Icon?>>()

  fun loadFavIcon(url: String, size: Int = 32, targetSize: Int = 16): CompletableFuture<Icon?> {
    return favIconCache.get(url) { loadImageAsync(url, size, targetSize) }
  }

  private fun loadImageAsync(url: String, size: Int, targetSize: Int): CompletableFuture<Icon?> {
    return CompletableFuture.supplyAsync {
      try {
        val domain = getDomainName(url.trim())
        val iconUrl = URL("https://www.google.com/s2/favicons?domain=$domain&sz=$size")
        ImageLoader.loadFromUrl(iconUrl)?.let { iconImage ->
          val iconScaled = ImageLoader.scaleImage(iconImage, targetSize)
          ImageIcon(iconScaled)
        }
      } catch (e: Exception) {
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
      url.removePrefix("www.").removeSuffix("/")
    }
  }

  override fun dispose() {
    favIconCache.cleanUp()
  }
}
