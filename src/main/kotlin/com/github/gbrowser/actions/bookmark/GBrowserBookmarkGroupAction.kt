package com.github.gbrowser.actions.bookmark


import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.settings.GBrowserSetting
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAware


class GBrowserBookmarkGroupAction : ActionGroup(GBrowserBundle.message("group.action.bookmarks"), true), DumbAware {

  init {
    isSearchable = true
    isEnabledInModalContext = true
    templatePresentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true)
    templatePresentation.icon = GBrowserIcons.BOOKMARK_MANAGER
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val actions = mutableListOf<GBrowserBookmarkGroupItemAction>()
    GBrowserSetting.instance().bookmarks.forEach { fav ->
      val url = fav.url
      val name = fav.name
      actions += GBrowserBookmarkGroupItemAction(name, url)
    }
    return actions.toTypedArray()
  }

}
