package com.github.gbrowser.actions.devtools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevTools
import com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevToolsFactory
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.Icon


class GBrowserDevToolsAction : AnAction() {
  private val icon: Icon = GBrowserIcons.DEV_TOOLS_ACTIVE
  private val iconActive: Icon = GBrowserIcons.DEV_TOOLS


  override fun update(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID)
    if (panel == null || toolWindow == null) {
      e.presentation.isEnabled = false
      e.presentation.icon = iconActive
      return
    }

    val existingContent = toolWindow.contentManager.contents.any {
      (it.component as? GBrowserToolWindowDevTools)?.browser?.id == panel.getBrowser().id
    }

    e.presentation.isEnabled = panel.hasContent()
    if (existingContent) {
      e.presentation.icon = icon
      e.presentation.text = "Show DevTools"
    } else {
      e.presentation.icon = iconActive
      e.presentation.text = "Open DevTools"
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
    panel.getDevToolsBrowser().let { browser ->
      val tabName = panel.getCurrentTitle()
      GBrowserToolWindowDevToolsFactory.Companion.createTab(project, browser, tabName)
    }

  }
}
