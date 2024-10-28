package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBrowser
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware


class GBrowserTabDuplicateAction : AnAction(), DumbAware {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.hasContent() == true
  }

  override fun actionPerformed(e: AnActionEvent) {
    val toolWindow = GBrowserToolWindowUtil.getToolWindow(e.project, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID) ?: return
    toolWindow.contentManager.selectedContent?.component?.let { component ->
      if (component is GBrowserToolWindowBrowser) {
        GBrowserToolWindowUtil.createContentTab(toolWindow, component.getCurrentUrl(), component.getCurrentTitle())
      }
    }
  }
}
