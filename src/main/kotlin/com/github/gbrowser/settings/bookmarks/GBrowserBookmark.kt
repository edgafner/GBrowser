package com.github.gbrowser.settings.bookmarks

import kotlinx.serialization.Serializable

@Serializable
data class GBrowserBookmark(var url: String = "", var name: String = "") {

  companion object {
    @Suppress("ConstPropertyName")
    const val serialVersionUID: Long = 12143532789876L
  }
}


