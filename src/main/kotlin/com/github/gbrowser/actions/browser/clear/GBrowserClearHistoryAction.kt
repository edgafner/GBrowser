package com.github.gbrowser.actions.browser.clear

import com.github.gbrowser.services.GBrowserService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

class GBrowserClearHistoryAction : AnAction(), DumbAware {


  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {

    e.presentation.isEnabled = e.project?.service<GBrowserService>()?.history?.isNotEmpty() ?: false
  }

  override fun actionPerformed(e: AnActionEvent) {
    e.project?.service<GBrowserService>()?.removeHistory()
  }
}
