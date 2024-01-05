package com.github.gbrowser.actions.browser

import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware


class GBrowserHomeAction  : AnAction(), DumbAware {

  

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT


  override fun actionPerformed(e: AnActionEvent) {
    val panel = getSelectedBrowserPanel(e) ?: return
    panel.loadDefaultUrl()
    panel.updateUI()
  }
}
