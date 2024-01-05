package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.settings.DateAsStringSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GBrowserTab(val url: String = "",
                       val name: String? = null,
                       @Serializable(with = DateAsStringSerializer::class) val createdAt: Date) {
  companion object {
    @Suppress("ConstPropertyName", "unused")
    const val serialVersionUID = 4423235970041806118L
  }
}
