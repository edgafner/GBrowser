package com.github.gbrowser.actions.browser

import com.github.gbrowser.settings.GBrowserSetting
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserClearHistoryAction : AnAction(), DumbAware {

  private val settings: GBrowserSetting = GBrowserSetting.instance()

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = settings.history.isNotEmpty()
  }

  override fun actionPerformed(e: AnActionEvent) {
    settings.removeHistory()
  }
}
