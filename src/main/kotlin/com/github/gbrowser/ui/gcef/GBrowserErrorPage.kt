package com.github.gbrowser.ui.gcef

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.JBColor
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.LazyInitializer
import org.cef.handler.CefLoadHandler
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*


interface GBrowserErrorPage {

  companion object {

    private val ERROR_PAGE_READER: LazyInitializer.LazyValue<String> = LazyInitializer.create { errorPageReaderLambda() }

    private fun errorPageReaderLambda(): String {
      return try {
        val bytes = FileUtil.loadBytes(Objects.requireNonNull(InputStream::class.java.getResourceAsStream("/loadError.html")))
        String(bytes, StandardCharsets.UTF_8)
      } catch (e: IOException) {
        Logger.getInstance(JBCefBrowserBase::class.java).error("couldn't find loadError.html", e)
        ""
      } catch (e: NullPointerException) {
        Logger.getInstance(JBCefBrowserBase::class.java).error("couldn't find loadError.html", e)
        ""
      }
    }

    fun create(errorCode: CefLoadHandler.ErrorCode, errorText: String, failedUrl: String): String = defaultLambda(errorCode, errorText,
                                                                                                                  failedUrl)

    @Suppress("UNUSED_PARAMETER")
    private fun defaultLambda(errorCode: CefLoadHandler.ErrorCode, errorText: String, failedUrl: String): String {
      val fontSize = (EditorColorsManager.getInstance().globalScheme.editorFontSize * 1.1).toInt()
      val headerFontSize = fontSize + JBUIScale.scale(3)
      val headerPaddingTop = headerFontSize / 5
      val lineHeight = headerFontSize * 2
      val iconPaddingRight = JBUIScale.scale(12)
      val bgColor = JBColor.background()
      val bgWebColor = "#%02x%02x%02x".format(bgColor.red, bgColor.green, bgColor.blue)
      val fgColor = JBColor.foreground()
      val fgWebColor = "#%02x%02x%02x".format(fgColor.red, fgColor.green, fgColor.blue)
      var html = ERROR_PAGE_READER.get()

      html = html.replace("\${lineHeight}", lineHeight.toString()).replace("\${iconPaddingRight}", iconPaddingRight.toString()).replace(
        "\${fontSize}", fontSize.toString()).replace("\${headerFontSize}", headerFontSize.toString()).replace("\${headerPaddingTop}",
                                                                                                              headerPaddingTop.toString()).replace(
        "\${bgWebColor}", bgWebColor).replace("\${fgWebColor}", fgWebColor).replace("\${errorText}", errorText).replace("\${failedUrl}",
                                                                                                                        failedUrl)
      return html
    }
  }
}
