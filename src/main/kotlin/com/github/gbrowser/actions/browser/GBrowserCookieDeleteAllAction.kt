package com.github.gbrowser.actions.browser

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAware

class GBrowserCookieDeleteAllAction : AnAction(),
                                      DumbAware {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    getSelectedBrowserPanel(e)?.deleteCookies()
  }
}
