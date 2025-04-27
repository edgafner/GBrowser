package com.github.gbrowser.actions.browser.navigation

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

/**
 * Represents an action for navigating backward.
 *
 * This action is only enabled when the associated browser panel can navigate backward.
 * It retrieves the currently active browser panel through the utility method and calls
 * the `goBack` method on the panel to perform the navigation.
 *
 * The action update thread is configured to run on the background thread (BGT).
 */
class GBrowserBackwardAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
      return
    }
    panel.canGoBack().let {
      e.presentation.isEnabled = it
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
    panel.goBack()
  }
}
