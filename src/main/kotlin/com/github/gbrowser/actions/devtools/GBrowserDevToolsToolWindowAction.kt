package com.github.gbrowser.actions.devtools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevToolsFactory
import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.github.gbrowser.ui.toolwindow.gbrowser.getToolWindow
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import javax.swing.Icon


class GBrowserDevToolsToolWindowAction : AnAction() {
  private val icon: Icon = GBrowserIcons.DEV_TOOLS
  private val iconActive: Icon = GBrowserIcons.DEV_TOOLS_ACTIVE


  override fun update(e: AnActionEvent) {
    val panel = getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
      e.presentation.icon = icon
      return
    }

    e.presentation.isEnabled = panel.hasContent()
    e.presentation.icon = if (panel.hasDevToolsInstance()) {
      iconActive
    } else {
      icon
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val panel = getSelectedBrowserPanel(e) ?: return

    val toolBrowser = panel.getDevToolsBrowser()
    if (toolBrowser != null) {
      if (panel.isDevToolsBrowserPanel()) {
        toolBrowser.setVisibility(true)
      } else {
        val toolWindow = getToolWindow(project, GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID) ?: return
        toolWindow.show()
      }

      return
    } else {
      panel.createDevTools(panel.getCurrentUrl())?.let { browser ->
        panel.setDevToolsBrowser(browser)
        val tabName = panel.getCurrentTitle()
        GBrowserToolWindowDevToolsFactory.Companion.createTab(project, browser, tabName)
      }
    }
  }
}
