package com.github.gbrowser.ui.toolwindow.dev_tools

import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent

class GBrowserDevToolsFocusListener() : FocusAdapter() {
  override fun focusGained(e: FocusEvent?) {
    // Focus handling is now managed by the browser component itself
    super.focusGained(e)
  }
}