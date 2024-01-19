package com.github.gbrowser.services.providers

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.JreHiDpiUtil
import com.intellij.util.ImageLoader
import com.intellij.util.JBHiDPIScaledImage
import java.net.URI
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.ImageIcon

@Service(Service.Level.APP)
class CachingFavIconLoader : Disposable {

  private val favIconCache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build<String, CompletableFuture<Icon?>>()

  fun loadFavIcon(url: String, size: Int = 64, targetSize: Int = 16): CompletableFuture<Icon?> {
    return favIconCache.get(url) { loadImageAsync(url, size, targetSize) }
  }

  private fun loadImageAsync(url: String, size: Int, targetSize: Int): CompletableFuture<Icon?> {
    return CompletableFuture.supplyAsync {
      try {
        val domain = getDomainName(url.trim())
        val iconUrl = URL("https://www.google.com/s2/favicons?domain=$domain&sz=$size")

        //ImageLoader.loadFromResource("/faviconV2.png", javaClass)?.let { iconImage ->
        //  val iconScaled = ImageLoader.scaleImage(iconImage, targetSize)
        //  ImageIcon(iconImage)
        //}
        ImageLoader.loadFromUrl(iconUrl)?.let { iconImage ->


          val originalIcon = if (JreHiDpiUtil.isJreHiDPIEnabled()) {
            logger<CachingFavIconLoader>().info("JreHiDpiUtil is enable")
            @Suppress("UnstableApiUsage")
            (iconImage as JBHiDPIScaledImage).delegate ?: return@let AllIcons.General.Web
          } else {
            iconImage
          }

          val iconScaled = ImageLoader.scaleImage(originalIcon, targetSize)
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
