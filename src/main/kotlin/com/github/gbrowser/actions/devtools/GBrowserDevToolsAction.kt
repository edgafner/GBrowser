package com.github.gbrowser.actions.devtools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import javax.swing.Icon

class GBrowserDevToolsAction : AnAction() {
  companion object {
    private val LOG = logger<GBrowserDevToolsAction>()
  }

  private val icon: Icon = GBrowserIcons.DEV_TOOLS_ACTIVE
  private val iconActive: Icon = GBrowserIcons.DEV_TOOLS

  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
      e.presentation.icon = icon
      return
    }

    e.presentation.isEnabled = panel.hasContent()
    e.presentation.icon = iconActive
    e.presentation.text = "Open DevTools"
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    LOG.debug("GBrowserDevToolsAction.actionPerformed() called")

    val project = e.getData(CommonDataKeys.PROJECT)
    if (project == null) {
      LOG.debug("Project is null, returning")
      return
    }

    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      LOG.debug("No browser panel selected, returning")
      return
    }

    // Check if the panel has loaded content
    if (!panel.hasContent()) {
      LOG.debug("Panel has no content, returning")
      return
    }

    try {
      LOG.debug("Opening DevTools for browser panel")

      // In the new API (253 EAP), DevTools can only be opened in a built-in dialog
      // The tool window option is no longer available
      LOG.debug("Opening DevTools in built-in dialog (new API)")
      val browser = panel.getBrowser()
      browser.openDevtools()

      LOG.debug("DevTools opened successfully")
    } catch (ex: Exception) {
      LOG.warn("Failed to open DevTools: ${ex.message}", ex)
    }
  }
}