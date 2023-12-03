package com.github.gbrowser.i18n

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

@NonNls
private const val BUNDLE = "messages.GBrowserBundle"


@Suppress("unused")
object GBrowserBundle : DynamicBundle(BUNDLE) {

  @Suppress("SpreadOperator")
  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = getMessage(key, *params)

  @Suppress("SpreadOperator")
  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<String> = getLazyMessage(key, *params)
}