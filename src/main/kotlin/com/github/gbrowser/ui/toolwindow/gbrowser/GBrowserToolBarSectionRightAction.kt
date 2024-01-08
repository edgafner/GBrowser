package com.github.gbrowser.ui.toolwindow.gbrowser


import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.actions.GBrowserDynamicGroupAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import javax.swing.JComponent

class GBrowserToolBarSectionRightAction(@Suppress("unused") private var parentComponent: JComponent) : AnAction(), CustomComponentAction,
                                                                                                       RightAlignedToolbarAction {

  override fun actionPerformed(eve: AnActionEvent) { // Implementation of action performed
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {

    val groupAll = GBrowserDynamicGroupAction(GBrowserActionId.allActions(), AllIcons.General.ArrowDown, "More Options")

    val actionGroup = DefaultActionGroup()
    actionGroup.add(GBrowserActionId.toAction(GBrowserActionId.GBROWSER_BOOKMARK_ADD_ID))
    actionGroup.add(groupAll)
    val bar = createToolBarActionPanel(actionGroup)
    return bar
  }


}
