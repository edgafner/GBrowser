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


/**
 * An action that provides a shortcut to open the browser bookmark manager.
 *
 * This action is enabled only when there is an active project and the browser service has bookmarks available.
 * It triggers the display of browser-related project settings with a focus on the bookmark manager.
 *
 * Inherits from `AnAction` and is marked as `DumbAware` to be available during the IDE's indexing process.
 */
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
