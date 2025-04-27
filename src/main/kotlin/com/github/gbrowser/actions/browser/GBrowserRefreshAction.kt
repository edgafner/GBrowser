package com.github.gbrowser.actions.browser

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserRefreshAction : AnAction(), DumbAware {
  
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.hasContent() == true
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
          panel.reload()
        }
}
