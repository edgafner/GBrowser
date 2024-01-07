package com.github.gbrowser.settings.dao

import kotlinx.serialization.Serializable

@Serializable
data class GBrowserHistory(val name: String, val url: String) {
  companion object {

    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID: Long = 12143532789876L
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GBrowserHistory

    return url == other.url
  }

  override fun hashCode(): Int {
    return url.hashCode()
  }
}



