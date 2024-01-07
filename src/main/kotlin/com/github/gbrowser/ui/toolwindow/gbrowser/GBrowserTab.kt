package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.services.DateAsStringSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GBrowserTab(val url: String = "",
                       val name: String = "",
                       @Serializable(with = DateAsStringSerializer::class) val createdAt: Date) {
  companion object {
    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID = 4423235970041806118L
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GBrowserTab

    return url == other.url
  }

  override fun hashCode(): Int {
    return url.hashCode()
  }
}
