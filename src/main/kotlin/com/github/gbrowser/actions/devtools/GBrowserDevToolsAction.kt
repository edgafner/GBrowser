package com.github.gbrowser.actions.devtools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import javax.swing.Icon

class GBrowserDevToolsAction : AnAction() {
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
    e.getData(CommonDataKeys.PROJECT) ?: return
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return

    // Check if the panel has loaded content
    if (!panel.hasContent()) {
      return
    }

    try { // Use the getDevToolsBrowser method which handles opening DevTools properly
      panel.getDevToolsBrowser()
    } catch (ex: Exception) { // Log error and handle gracefully
      com.intellij.openapi.diagnostic.Logger.getInstance(GBrowserDevToolsAction::class.java).warn("Failed to open DevTools: ${ex.message}", ex)
    }
  }
}