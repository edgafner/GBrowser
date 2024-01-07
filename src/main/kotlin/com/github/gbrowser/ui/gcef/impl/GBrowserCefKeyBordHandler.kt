package com.github.gbrowser.ui.gcef.impl

import org.cef.browser.CefBrowser
import org.cef.handler.CefKeyboardHandler
import org.cef.misc.BoolRef

class GBrowserCefKeyBordHandler : CefKeyboardHandler {
  override fun onPreKeyEvent(p0: CefBrowser?, p1: CefKeyboardHandler.CefKeyEvent?, p2: BoolRef?): Boolean {
    return true
  }

  override fun onKeyEvent(p0: CefBrowser?, p1: CefKeyboardHandler.CefKeyEvent?): Boolean {
    if (p0 == null) {
      return true
    }

    val isCharPlus = p1?.character == '+'
    val isCharMinus = p1?.character == '-'
    val isCharZero = p1?.character == '0'
    val isCharR = p1?.character == 'r'
    val isCtrl = p1?.modifiers == 128

    when {
      isCtrl && isCharPlus -> {
        p0.zoomLevel += 1.0
      }
      isCtrl && isCharMinus -> {
        p0.zoomLevel -= 1.0
      }
      isCtrl && isCharZero -> {
        p0.zoomLevel = 0.0
      }
      isCtrl && isCharR -> {
        p0.devTools.reload()
      }
    }

    return true
  }
}
