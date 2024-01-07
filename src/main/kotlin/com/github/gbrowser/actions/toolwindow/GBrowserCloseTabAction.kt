package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware


class GBrowserCloseTabAction : AnAction(), DumbAware {

  init {
    isEnabledInModalContext = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun actionPerformed(event: AnActionEvent) {
    GBrowserToolWindowUtil.getToolWindow(event.project, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)?.let { toolWindow ->
      toolWindow.contentManager.selectedContent?.let { content ->
        toolWindow.contentManager.removeContent(content, true)
      }
    }
  }
}
