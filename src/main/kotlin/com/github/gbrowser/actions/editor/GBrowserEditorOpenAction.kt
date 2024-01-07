package com.github.gbrowser.actions.editor

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GBrowserEditorOpenAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = GBrowserUtil.getSelectedText(e) != null
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(anActionEvent: AnActionEvent) {
    GBrowserToolWindowUtil.createContentTabAndShow(anActionEvent, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID,
                                                   GBrowserUtil.getSelectedText(anActionEvent)!!)
  }
}
