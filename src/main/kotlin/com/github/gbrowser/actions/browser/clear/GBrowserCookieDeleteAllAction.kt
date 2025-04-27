package com.github.gbrowser.actions.browser.clear

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

/**
 * An action that clears all cookies in the GBrowserToolWindow.
 *
 * This action is enabled when the user selects a browser panel in the tool window.
 * When performed, it instructs the selected browser panel to delete all cookies.
 *
 * Implement DumbAware to ensure the action can be executed in dumb mode.
 */
class GBrowserCookieDeleteAllAction : AnAction(),
                                      DumbAware {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.deleteCookies()
  }
}
