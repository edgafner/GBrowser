package com.github.gbrowser.actions.bookmark


import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon


class GBrowserBookmarkGroupAction : ActionGroup(GBrowserBundle.message("group.action.bookmarks"), true), DumbAware {
  private val favIconLoader: CachingFavIconLoader = service()

  init {
    isSearchable = true
    isEnabledInModalContext = true
    templatePresentation.putClientProperty(ActionUtil.HIDE_DROPDOWN_ICON, true)
    templatePresentation.icon = GBrowserIcons.BOOKMARK_MANAGER
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val project = e?.project ?: return emptyArray()
    val actions = mutableListOf<GBrowserBookmarkGroupItemAction>()
    project.service<GBrowserService>().bookmarks.forEach { fav ->
      val url = fav.url
      val name = fav.name
      favIconLoader.loadFavIcon(url).thenAccept {
        actions += GBrowserBookmarkGroupItemAction(name, url, it ?: AllIcons.General.Web)
      }
    }
    return actions.toTypedArray()
  }

}

class GBrowserBookmarkGroupItemAction(val name: String, val url: String, val icon: Icon) : AnAction(name) {


  override fun update(e: AnActionEvent) {
    e.presentation.icon = icon
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(anActionEvent: AnActionEvent) {
    val project = anActionEvent.project ?: return
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(anActionEvent)
    val isDefaultUrlLoaded = panel?.getCurrentUrl() == project.service<GBrowserService>().defaultUrl

    if (isDefaultUrlLoaded) {
      panel.loadUrl(url)
    } else {
      GBrowserToolWindowUtil.createContentTab(anActionEvent, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID, url)
    }
  }
}
