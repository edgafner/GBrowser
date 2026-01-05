package com.github.gbrowser.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.GBrowserBundle"

/**
 * Bundle for internationalization messages in GBrowser plugin.
 */
object GBrowserBundle : DynamicBundle(BUNDLE) {

  /**
   * Retrieves a localized message from the bundle.
   * @param key The message key
   * @param params Optional parameters for message formatting
   * @return The localized message string
   */
  @JvmStatic
  fun message(key: @PropertyKey(resourceBundle = BUNDLE) @NonNls String, vararg params: Any): @Nls String = getMessage(key, *params)

}
