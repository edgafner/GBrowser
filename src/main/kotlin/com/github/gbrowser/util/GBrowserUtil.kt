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

/**
 * Utility object for GBrowser plugin containing common constants and helper functions.
 */
object GBrowserUtil {
  const val GBROWSER_TOOL_WINDOW_ID = "GBrowser"
  // DevTools tool window removed - no longer available in new API (253 EAP)

  val LOG = logger<GBrowserUtil>()

  /**
   * Fetches search query suggestions from Google.
   * @param text The search text to get suggestions for
   * @return JSON response containing search suggestions
   */
  internal fun suggestQuery(text: String): String {
    if (text.isEmpty()) return ""
    val url = "https://suggestqueries.google.com/complete/search?client=firefox&q=$text"
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    client.newCall(request).execute().use { response ->
      return response.body.string()
    }
  }

  /**
   * Gets the JCEF debug port from the IDE registry.
   * @return The current JCEF debug port number
   */
  fun getJCEFDebugPort(): Int {
    return Registry.get("ide.browser.jcef.debug.port").asInteger()
  }

  /**
   * Sets the JCEF debug port in the IDE registry.
   * @param port The port number to set
   */
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

  /**
   * Validates if the input string is a valid browser URL.
   * Checks for valid URL format or localhost references.
   * @param input The string to validate
   * @return true if the input is a valid browser URL, false otherwise
   */
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

  /**
   * Gets the selected text from the current editor if it's a valid URL.
   * @param anActionEvent The action event containing editor data
   * @return The selected text if it's a valid URL, null otherwise
   */
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
