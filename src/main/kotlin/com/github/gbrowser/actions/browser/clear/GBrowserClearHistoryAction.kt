package com.github.gbrowser.actions.browser.clear

import com.github.gbrowser.services.GBrowserService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

/**
 * An action that clears the browsing history in the GBrowserService of the current project.
 *
 * This action checks if the browsing history contains any entries and enables or disables itself
 * based on the presence of such entries. When performed, it instructs the GBrowserService to
 * clear the browsing history.
 *
 * Implement DumbAware to ensure the action can be executed in dumb mode.
 */
class GBrowserClearHistoryAction : AnAction(), DumbAware {


  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {

    e.presentation.isEnabled = e.project?.service<GBrowserService>()?.history?.isNotEmpty() ?: false
  }

  override fun actionPerformed(e: AnActionEvent) {
    e.project?.service<GBrowserService>()?.removeHistory()
  }
}
