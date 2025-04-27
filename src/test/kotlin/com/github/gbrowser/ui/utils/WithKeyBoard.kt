package com.github.gbrowser.ui.utils

import com.intellij.driver.sdk.ui.keyboard.WithKeyboard
import com.intellij.openapi.util.SystemInfoRt
import java.awt.event.KeyEvent

fun WithKeyboard.cleanEnterTextEnter(text: String) {
  keyboard {
    backspace()
    typeText(text)
    enter()
  }
}

fun WithKeyboard.selectAll() {
  keyboard {
    if (SystemInfoRt.isMac) {
      hotKey(KeyEvent.VK_META, KeyEvent.VK_A)
    } else {
      hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_A)
    }
  }
}
