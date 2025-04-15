package com.github.gbrowser.ui.utils

import com.intellij.driver.sdk.ui.keyboard.WithKeyboard
import com.intellij.openapi.util.SystemInfoRt
import java.awt.event.KeyEvent

fun WithKeyboard.escape() {
  keyboard {
    escape()
  }
}

fun WithKeyboard.enter() {
  keyboard {
    enter()
  }
}

fun WithKeyboard.enterTextEnter(text: String) {
  keyboard {
    typeText(text)
    enter()
  }
}

fun WithKeyboard.enterEnterText(text: String) {
  keyboard {
    enter()
    typeText(text)
  }
}

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


fun WithKeyboard.cleanEnterTextDownEnter(text: String) {
  keyboard {
    backspace()
    typeText(text)
    down()
    enter()
  }
}