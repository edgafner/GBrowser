package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBrowser
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBuilder
import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.github.gbrowser.ui.toolwindow.gbrowser.getToolWindow
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware


class GBrowserTabDuplicateAction : AnAction(), DumbAware {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = getSelectedBrowserPanel(e)?.hasContent() ?: false
  }

  override fun actionPerformed(e: AnActionEvent) {
    val toolWindow = getToolWindow(e.project, GBrowserUtil.GROUP_DISPLAY_ID) ?: return
    toolWindow.contentManager.selectedContent?.component?.let { component ->
      if (component is GBrowserToolWindowBrowser) {
        GBrowserToolWindowBuilder.createContentTab(toolWindow, component.getCurrentUrl(), component.getCurrentTitle())
      }
    }
  }
}
