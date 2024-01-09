package com.github.gbrowser.actions.browser.navigation

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserBackwardAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
      return
    }
    panel.canGoBack().let {
      e.presentation.isEnabled = it
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
    panel.goBack()
  }
}
