package com.github.gbrowser.settings.request_header

import kotlinx.serialization.Serializable

@Serializable
data class GBrowserRequestHeader(var value: String = "", var name: String? = null, var overwrite: Boolean = false,var uriRegex: String = "") {
  companion object {
    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID: Long = 3523235970041806118L
  }
}