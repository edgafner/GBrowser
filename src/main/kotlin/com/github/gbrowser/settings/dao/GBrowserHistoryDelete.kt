package com.github.gbrowser.settings.dao

import com.github.gbrowser.settings.DateAsStringSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GBrowserHistoryDelete(val hours: Int, val displayText: String) {
  companion object {
    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID = 3523235970041806118L
  }

  override fun toString(): String {
    return displayText
  }
}

@Serializable
data class GBrowserHistory(val name: String, val url: String, @Serializable(with = DateAsStringSerializer::class) val createdAt: Date) {
  companion object {

    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID: Long = 12143532789876L
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GBrowserHistory

    if (url != other.url) return false

    return true
  }

  override fun hashCode(): Int {
    return url.hashCode()
  }
}



