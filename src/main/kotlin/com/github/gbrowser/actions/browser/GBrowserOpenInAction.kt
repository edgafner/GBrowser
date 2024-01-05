package com.github.gbrowser.actions.browser

import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.browsers.actions.findUsingBrowser
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserOpenInAction  : AnAction(), DumbAware {

  

  private val defaultBrowser by lazy { findUsingBrowser() }
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = getSelectedBrowserPanel(e)?.hasContent() ?: false
    defaultBrowser?.let {
      e.presentation.icon = it.icon
    }

    if (defaultBrowser == null) {
      e.presentation.icon = AllIcons.RunConfigurations.Web_app
    }

  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    getSelectedBrowserPanel(e)?.getCurrentUrl()?.let { BrowserUtil.browse(it) }
  }
}
