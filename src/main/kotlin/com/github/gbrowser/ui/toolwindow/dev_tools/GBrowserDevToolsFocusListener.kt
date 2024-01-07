package com.github.gbrowser.ui.toolwindow.dev_tools

import com.github.gbrowser.ui.gcef.impl.GBrowserCefKeyBordHandler
import org.cef.CefClient
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.util.*

class GBrowserDevToolsFocusListener(private val client: CefClient) : FocusAdapter() {
  private var lastDate: Date = Date()
  private var waitBeforeNext: Boolean = false

  private fun preventFocusFlicker(e: FocusEvent?) {
    val date = Date()
    val duration = date.time - lastDate.time
    if (!waitBeforeNext && duration in 1..50) {
      waitBeforeNext = true
      e?.component?.isFocusable = false
      Timer().schedule(object : TimerTask() {
        override fun run() {
          e?.component?.isFocusable = true
          waitBeforeNext = false
        }
      }, 50)
    }
    lastDate = date
  }

  override fun focusGained(e: FocusEvent?) {
    client.removeKeyboardHandler()
    client.addKeyboardHandler(GBrowserCefKeyBordHandler())
  }

  override fun focusLost(e: FocusEvent?) {
    preventFocusFlicker(e)
    client.removeKeyboardHandler()
  }
}
