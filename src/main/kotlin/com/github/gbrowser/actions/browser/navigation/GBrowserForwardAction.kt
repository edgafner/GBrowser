package com.github.gbrowser.actions.browser.navigation

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

/**
 * Represents an action for navigating forward in a browser panel.
 *
 * This action is only enabled when the associated browser panel can perform a forward navigation.
 * It uses the `GBrowserToolWindowUtil` utility to fetch the currently active browser panel and then
 * invokes the `goForward` method on this panel to execute the forward navigation.
 *
 * The action update thread is configured to run on the background thread (BGT).
 */
class GBrowserForwardAction : AnAction(), DumbAware {


  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
      return
    }
    panel.canGoForward().let {
      e.presentation.isEnabled = it
    }

  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
    panel.goForward()
  }
}
