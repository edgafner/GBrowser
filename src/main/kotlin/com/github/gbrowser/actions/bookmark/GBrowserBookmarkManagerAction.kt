package com.github.gbrowser.actions.bookmark

import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.settings.project.GBrowserProjectSettingsConfigurable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware

class GBrowserBookmarkManagerAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = GBrowserSetting.instance().bookmarks.isNotEmpty()
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    ApplicationManager.getApplication().invokeLater {
      val settingsInstance = ShowSettingsUtil.getInstance()
      val configurable = GBrowserProjectSettingsConfigurable()
      configurable.openCollapseBookMarks(true)
      settingsInstance.editConfigurable(e.project, configurable.id, configurable, true)
    }
  }
}
