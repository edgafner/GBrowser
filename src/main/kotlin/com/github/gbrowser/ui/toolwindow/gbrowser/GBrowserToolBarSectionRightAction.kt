package com.github.gbrowser.ui.toolwindow.gbrowser


import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.actions.GBrowserDynamicGroupAction
import com.github.gbrowser.actions.bookmark.GBrowserBookmarkGroupAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import javax.swing.JComponent

class GBrowserToolBarSectionRightAction(@Suppress("unused") private var parentComponent: JComponent) : AnAction(), CustomComponentAction,
                                                                                                       RightAlignedToolbarAction {

  override fun actionPerformed(eve: AnActionEvent) { // Implementation of action performed
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    val actionGroup = DefaultActionGroup()
    actionGroup.addAll(GBrowserActionId.RIGHT)

    val listAllAction = mutableListOf<AnAction>()
    val devToolsGroup = GBrowserDynamicGroupAction(GBrowserActionId.DEVTOOLS_GROUP, GBrowserIcons.DEV_TOOLS, "Inspect")
    listAllAction.add(devToolsGroup)
    listAllAction.add(Separator.create())

    val cleanGroup = GBrowserDynamicGroupAction(GBrowserActionId.CLEAR_BROWSER_DATA, GBrowserIcons.COOKIES, "Clear Cookies and History")
    listAllAction.add(cleanGroup)
    listAllAction.add(Separator.create())
    listAllAction.addAll(GBrowserActionId.BOOKMARK)
    listAllAction.add(GBrowserBookmarkGroupAction())
    listAllAction.add(Separator.create())
    listAllAction.addAll(GBrowserActionId.BROWSER)
    listAllAction.add(Separator.create())
    listAllAction.addAll(GBrowserActionId.ZOOM)
    listAllAction.add(Separator.create())
    listAllAction.add(GBrowserActionId.toAction(GBrowserActionId.GBROWSER_PREFERENCES_ID))

    val groupAll = GBrowserDynamicGroupAction(listAllAction, AllIcons.General.ArrowDown, "More Options")

    actionGroup.add(groupAll)
    val bar = createToolBarActionPanel(actionGroup)
    return bar
  }


}
