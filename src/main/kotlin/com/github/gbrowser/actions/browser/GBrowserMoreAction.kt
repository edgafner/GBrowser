package com.github.gbrowser.actions.browser

import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware


class GBrowserFindAction  : AnAction(), DumbAware {

  

  override fun update(e: AnActionEvent) {
    val panel = getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
    }
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val browser = getSelectedBrowserPanel(e)?.getBrowser() ?: return
    val project = e.getRequiredData(CommonDataKeys.PROJECT)
    val findDialog = GBrowserFindDialog(project, browser)

    if (!findDialog.isVisible) {
      findDialog.setDialogLocation()
    }

    findDialog.show()
  }
}