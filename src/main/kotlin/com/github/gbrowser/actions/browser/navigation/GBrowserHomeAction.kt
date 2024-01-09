package com.github.gbrowser.actions.browser.navigation

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware


class GBrowserHomeAction : AnAction(), DumbAware {


  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT


  override fun actionPerformed(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return

    panel.loadUrl(GBrowserService.instance().defaultUrl)
  }
}
