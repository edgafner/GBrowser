package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserToggleToolbarAction : AnAction(), DumbAware {


  override fun update(e: AnActionEvent) {
    val contentManager = GBrowserToolWindowUtil.getContentManager(e.project, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)
    e.presentation.isEnabled = contentManager?.isEmpty?.not() == true
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.let { panel ->
      val isVisible = panel.isToolBarVisible()
      panel.setToolBarVisible(!isVisible)
    }
  }
}
