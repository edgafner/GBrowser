package com.github.gbrowser.actions.bookmark


import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon


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
    GBrowserService.instance().bookmarks.forEach { fav ->
      val url = fav.url
      val name = fav.name
      actions += GBrowserBookmarkGroupItemAction(name, url)
    }
    return actions.toTypedArray()
  }

}

class GBrowserBookmarkGroupItemAction(val name: String, val url: String, initialIcon: Icon = AllIcons.General.Web) : AnAction({ name }) {

  private val settings: GBrowserService = GBrowserService.instance()
  private val favIconLoader: CachingFavIconLoader = service()

  var icon: Icon = initialIcon
    private set // Make the setter private

  init {
    if (initialIcon == AllIcons.General.Web) {
      loadUrlIcon()
    }
  }

  private fun loadUrlIcon() {
    val taskTitle = "loadFavIcon_$url"
    ProgressManager.getInstance().run(object : Task.Backgroundable(null, taskTitle) {
      override fun run(indicator: ProgressIndicator) {
        favIconLoader.loadFavIcon(url).thenAccept {
          updateIcon(it ?: AllIcons.General.Web)
        }
      }
    })
  }

  private fun updateIcon(newIcon: Icon) {
    icon = newIcon
  }

  override fun update(e: AnActionEvent) {
    e.presentation.icon = icon
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(anActionEvent: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(anActionEvent)
    val isDefaultUrlLoaded = panel?.getCurrentUrl() == settings.defaultUrl

    if (isDefaultUrlLoaded) {
      panel?.loadUrl(url)
    } else {
      GBrowserToolWindowUtil.createContentTab(anActionEvent, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID, url)
    }
  }
}
