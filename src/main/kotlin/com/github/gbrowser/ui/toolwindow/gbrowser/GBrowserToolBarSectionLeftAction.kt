package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.actions.GBrowserActionId
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import javax.swing.JComponent

class GBrowserToolBarSectionLeftAction(@Suppress("unused") private var parentComponent: JComponent) : AnAction(), CustomComponentAction {

  override fun actionPerformed(e: AnActionEvent) { // Implementation of action performed
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    val actionGroup = DefaultActionGroup()
    actionGroup.addAll(GBrowserActionId.LEFT)

    return createToolBarActionPanel(actionGroup)

  }


}
