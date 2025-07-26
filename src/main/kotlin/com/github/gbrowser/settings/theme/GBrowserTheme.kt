package com.github.gbrowser.settings.theme

enum class GBrowserTheme(val displayName: String) {
  FOLLOW_IDE("Follow IDE"),
  LIGHT("Light"),
  DARK("Dark");
    
  companion object {
    fun fromString(value: String?): GBrowserTheme {
      return GBrowserTheme.entries.find { it.name == value } ?: FOLLOW_IDE
    }
  }
}