package com.github.gbrowser.settings.bookmarks

import kotlinx.serialization.Serializable

@Serializable
data class GBrowserBookmark(var url: String = "", var name: String = "") {

  companion object {
    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID: Long = 12143532789876L
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GBrowserBookmark) return false

    if (url != other.url) return false

    return true
  }

  override fun hashCode(): Int {
    return url.hashCode()
  }


}


