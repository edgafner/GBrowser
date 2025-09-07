package com.github.gbrowser.actions.browser

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware

/**
 * Represents an action to invoke the "Find" functionality within the browser tool window in the IDE.
 * This action enables users to search for text within a browser component embedded in the IDE.
 *
 * The action works in the context of selecting a browser panel from the tool window and displaying
 * a non-modal custom search dialog to facilitate search operations.
 */
class GBrowserFindAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
    }
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val browser = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.getBrowser() ?: return
    val project = e.getData(CommonDataKeys.PROJECT) ?: return
    val findDialog = GBrowserFindDialog(project, browser)

    findDialog.show()
    findDialog.setDialogLocation()
  }
}