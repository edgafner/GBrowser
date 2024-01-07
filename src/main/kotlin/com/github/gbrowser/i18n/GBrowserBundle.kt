package com.github.gbrowser.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.GBrowserBundle"


object GBrowserBundle : DynamicBundle(BUNDLE) {

  @JvmStatic
  fun message(key: @PropertyKey(resourceBundle = BUNDLE) @NonNls String, vararg params: Any): @Nls String = getMessage(key, *params)

}
