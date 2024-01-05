package com.github.gbrowser.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon


class GBrowserDynamicGroupAction(private val actionItems: List<AnAction>, val icon: Icon, val text: String) : ActionGroup(text, null, icon),
                                                                                                              DumbAware {

  init {
    isPopup = true
    isSearchable = true
    templatePresentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = actionItems.isNotEmpty()
  }

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    return actionItems.toTypedArray()
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}
