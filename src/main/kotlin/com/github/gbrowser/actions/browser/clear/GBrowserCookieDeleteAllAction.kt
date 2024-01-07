package com.github.gbrowser.actions.browser.clear

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

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
