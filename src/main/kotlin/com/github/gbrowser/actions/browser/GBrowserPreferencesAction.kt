package com.github.gbrowser.actions.browser

import com.github.gbrowser.settings.project.GBrowserProjectSettingsConfigurable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware


class GBrowserPreferencesAction : AnAction(), DumbAware {

  init {
    this.isEnabledInModalContext = true
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    ApplicationManager.getApplication().invokeLater {
      val settingsInstance = ShowSettingsUtil.getInstance()
      val configurable = GBrowserProjectSettingsConfigurable(project)
      configurable.openCollapseBookMarks()
      settingsInstance.editConfigurable(project, configurable.id, configurable as Configurable, true)
    }
  }
}
