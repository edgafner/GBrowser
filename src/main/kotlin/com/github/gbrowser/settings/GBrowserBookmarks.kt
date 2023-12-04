package com.github.gbrowser.settings

import java.io.Serializable
import java.util.*

class GBrowserBookmarks : Serializable {
  companion object {

    @Suppress("ConstPropertyName")
    const val serialVersionUID: Long = 12143532789876L
  }

  var webUrl: String = ""

  @Suppress("unused")
  constructor() {
    // Need it for serialization
  }

  constructor(webUrl: String) {
    this.webUrl = webUrl
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }
    val that = other as GBrowserBookmarks
    return Objects.equals(webUrl, that.webUrl)
  }

  override fun hashCode(): Int {
    return webUrl.hashCode()
  }


}