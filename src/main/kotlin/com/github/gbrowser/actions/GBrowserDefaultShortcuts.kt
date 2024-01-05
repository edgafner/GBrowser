package com.github.gbrowser.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ShortcutSet
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.util.SystemInfo
import java.awt.event.KeyEvent.*
import javax.swing.JComponent
import javax.swing.KeyStroke


object GBrowserShortcutBuilder {

  private val registeredShortCuts = emptyList<Int>()

  fun registerShortcut(shortCut: ShortcutSet?, action: AnAction, id: Int, component: JComponent?) {
    if (!registeredShortCuts.contains(id)) {
      shortCut?.let { sc ->
        component?.let { comp ->
          action.registerCustomShortcutSet(sc, comp)
          registeredShortCuts.plus(id)
        }
      }
    }
  }

}


