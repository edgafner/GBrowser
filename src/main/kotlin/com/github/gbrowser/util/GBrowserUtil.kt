package com.github.gbrowser.util

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.ImageLoader
import com.intellij.util.ui.HTMLEditorKitBuilder
import com.intellij.util.ui.JBImageIcon
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.net.URL
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.Icon
import javax.swing.text.html.HTMLDocument

object GBrowserUtil {
  const val GROUP_DISPLAY_ID = "GBrowser"
  const val DEVTOOLS_TOOL_WINDOW_ID = "DevTools"

  fun getTitleOfWebPage(url: String): String {

    return try {
      val connection = URL(url).openConnection()

      val editorKit = HTMLEditorKitBuilder().withGapsBetweenParagraphs().withoutContentCss().build()
      val htmlDoc = editorKit.createDefaultDocument()
      editorKit.read(connection.getInputStream(), htmlDoc, 0)

      // Extract title
      val res = htmlDoc.getProperty(HTMLDocument.TitleProperty) as? String
      res ?: "Unknown"
    } catch (e: Exception) {
      "Unknown"
    }
  }


  private fun getDomainName(url: String): String {
    return try {
      URI(url).host?.removePrefix("www.")?.removeSuffix("/") ?: url
    } catch (e: Exception) {
      url.removePrefix("www.").removeSuffix("/")
    }
  }

  fun loadFavIconBGT(url: String, onSuccess: (Icon?) -> Unit) {
    ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Load Suggestion Search FavIcon_$url") {
      private var icon: Icon? = null

      override fun run(indicator: ProgressIndicator) {
        icon = loadFavIcon(url, 32, 18)
      }

      override fun onFinished() {
        icon?.let { onSuccess(it) }
        super.onFinished()
      }
    })
  }

  fun loadFavIconBGTSmall(url: String, onSuccess: (Icon?) -> Unit) {
    ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Load Suggestion Search FavIcon_$url") {
      private var icon: Icon? = null

      override fun run(indicator: ProgressIndicator) {
        icon = loadFavIcon(url, 32, 16)
      }

      override fun onFinished() {
        icon?.let { onSuccess(it) }
        super.onFinished()
      }
    })
  }

  fun loadFavIcon(url: String, size: Int = 32, targetSize: Int = 16): Icon? {
    return try {
      val domain = getDomainName(url.trim())
      val iconUrl = URL("https://www.google.com/s2/favicons?domain=$domain&sz=$size")
      ImageLoader.loadFromUrl(iconUrl)?.let { iconImage ->
        val iconScaled = ImageLoader.scaleImage(iconImage, targetSize)
        JBImageIcon(iconScaled)
      }
    } catch (e: Exception) {
      null
    }
  }

  fun suggestQuery(text: String): String {
    if (text.isEmpty()) return ""
    val url = "https://suggestqueries.google.com/complete/search?client=firefox&q=$text"
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    client.newCall(request).execute().use { response ->
      return response.body?.string() ?: ""
    }
  }

  fun getJCEFDebugPort(): Int {
    return Registry.get("ide.browser.jcef.debug.port").asInteger()
  }

  fun setJCEFDebugPort(port: Int) {
    Registry.get("ide.browser.jcef.debug.port").setValue(port)
  }


  private fun isValidUrlWithoutProtocol(input: String): Boolean {
    return try {
      val regex =
        """\b(?:(https?|ftp|file)://|www\.)?[-A-Z0-9+&#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]""" + """\.[-A-Z0-9+&@#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]"""
      val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE)
      val matcher = pattern.matcher(input)
      matcher.matches()
    } catch (e: PatternSyntaxException) {
      false
    }
  }

  fun isValidBrowserURL(input: String): Boolean {
    return try {
      val isValidUrl = isValidUrlWithoutProtocol(input)
      val isLocalhost = input.contains("localhost", ignoreCase = true)
      isValidUrl || isLocalhost
    } catch (e: PatternSyntaxException) {
      false
    }
  }


}
