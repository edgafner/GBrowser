package com.github.gbrowser.ui.search

import javax.swing.Icon

data class GBrowserSearchPopUpItem(val highlight: String,
                                   var icon: Icon?,
                                   var info: String?,
                                   val isURLVisible: Boolean,
                                   var name: String,
                                   var url: String) {

  fun matchesText(text: String): Boolean {
    val searchText = text.lowercase()
    return name.lowercase().contains(searchText) || url.lowercase().contains(searchText)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GBrowserSearchPopUpItem) return false

    if (name != other.name) return false
    if (url != other.url) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + url.hashCode()
    return result
  }
}
