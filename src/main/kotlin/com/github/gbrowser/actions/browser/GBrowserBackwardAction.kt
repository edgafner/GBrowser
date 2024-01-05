package com.github.gbrowser.actions.browser

import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserBackwardAction  : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    val panel = getSelectedBrowserPanel(e)
    e.presentation.isEnabled = panel?.canGoBack() ?: false
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val panel = getSelectedBrowserPanel(e) ?: return
    panel.goBack()
    panel.updateUI()
  }
}
