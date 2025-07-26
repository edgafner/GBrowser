package com.github.gbrowser.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.util.registry.Registry
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object GBrowserUtil {
  const val GBROWSER_TOOL_WINDOW_ID = "GBrowser"
  const val DEVTOOLS_TOOL_WINDOW_ID = "GDevTools"

  val LOG = logger<GBrowserUtil>()

  internal fun suggestQuery(text: String): String {
    if (text.isEmpty()) return ""
    val url = "https://suggestqueries.google.com/complete/search?client=firefox&q=$text"
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    client.newCall(request).execute().use { response ->
      return response.body.string()
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
      LOG.warn("PatternSyntaxException occurred when validating URL: $input", e)
      false
    }
  }

  fun isValidBrowserURL(input: String): Boolean {
    return try {
      val isValidUrl = isValidUrlWithoutProtocol(input)
      val isLocalhost = input.contains("localhost", ignoreCase = true)
      isValidUrl || isLocalhost
    } catch (e: PatternSyntaxException) {
      LOG.warn("PatternSyntaxException occurred when validating URL: $input", e)
      false
    }
  }

  fun getSelectedText(anActionEvent: AnActionEvent): String? {
    val editor = anActionEvent.getData(CommonDataKeys.EDITOR) ?: return null

    if (editor.editorKind != EditorKind.MAIN_EDITOR) return null

    val selected = editor.selectionModel.selectedText ?: return null

    if (selected.isBlank()) return null

    val trimmed = selected.trim()
    if (isValidBrowserURL(trimmed)) return trimmed

    return null
  }


}
