package com.github.gbrowser.listeners

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.theme.GBrowserTheme
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager

class GBrowserThemeListener : LafManagerListener {
  override fun lookAndFeelChanged(source: LafManager) {
    // When IDE theme changes, refresh all browsers that are set to "Follow IDE"
    for (project in ProjectManager.getInstance().openProjects) {
      val settings = project.service<GBrowserService>()
      if (settings.theme == GBrowserTheme.FOLLOW_IDE) {
        refreshProjectBrowsers(project)
      }
    }
  }

  private fun refreshProjectBrowsers(project: Project) {
    GBrowserToolWindowUtil.getAllBrowsers(project).forEach { browser ->
      browser.refreshTheme()
    }
  }
}