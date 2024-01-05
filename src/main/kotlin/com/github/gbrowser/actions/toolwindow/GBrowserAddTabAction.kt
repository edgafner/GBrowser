package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBuilder
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager.Companion.getInstance

class GBrowserAddTabAction : AnAction(), DumbAware {

  init {
    isEnabledInModalContext = true

  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    getInstance(project).getToolWindow(GBrowserUtil.GROUP_DISPLAY_ID)?.let {
      GBrowserToolWindowBuilder.createContentTab(it, GBrowserSetting.instance().defaultUrl, "")
    }
  }
}

