package com.github.gbrowser.actions.bookmark

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.project.GBrowserProjectSettingsConfigurable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware

class GBrowserBookmarkManagerAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.project?.service<GBrowserService>()?.bookmarks?.isNotEmpty() ?: false
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    ApplicationManager.getApplication().invokeLater {
      val settingsInstance = ShowSettingsUtil.getInstance()
      val configurable = GBrowserProjectSettingsConfigurable(project)
      configurable.openCollapseBookMarks(true)
      settingsInstance.editConfigurable(project, configurable.id, configurable, true)
    }
  }
}
