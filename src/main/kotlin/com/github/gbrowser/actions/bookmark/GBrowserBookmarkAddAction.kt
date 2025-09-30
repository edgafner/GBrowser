package com.github.gbrowser.actions.bookmark

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

/**
 * Represents an action to add or remove a bookmark for the current URL in a custom browser tool window.
 *
 * This action dynamically updates its presentation (icon and text) based on whether the current URL
 * has already been bookmarked or not.
 * It interacts with the custom `GBrowserService` to manage
 * bookmarks and updates the state accordingly.
 */
class GBrowserBookmarkAddAction : AnAction(), DumbAware {
  private val iconAdd: Icon = GBrowserIcons.BOOKMARK_REMOVE
  private val iconExist: Icon = GBrowserIcons.BOOKMARK_ADD
  private val textAdd: String = GBrowserBundle.message("actions.bookmark.add.text")
  private val textRemove: String = GBrowserBundle.message("actions.bookmark.remove.text")


  override fun update(e: AnActionEvent) {

    val project = e.project
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) //if (panel == null || !panel.hasContent()) {
    if (panel == null || project == null) {
      e.presentation.isEnabled = false
      return
    }

    e.presentation.isEnabled = true
    val currentUrl = panel.getCurrentUrl()
    val existBookmarks = project.service<GBrowserService>().existBookmark(currentUrl)

    e.presentation.icon = if (existBookmarks) iconExist else iconAdd
    e.presentation.text = if (existBookmarks) textRemove else textAdd
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
    val settings = project.service<GBrowserService>()
    val currentUrl = panel.getCurrentUrl()
    val existBookmarks = settings.existBookmark(currentUrl)

    val favorite = GBrowserBookmark(currentUrl, panel.getCurrentTitle())
    if (existBookmarks) {
      settings.removeBookmark(favorite)
    } else {
      settings.addBookmarks(favorite)
    }

    // Update action presentation immediately to reflect new state
    e.presentation.icon = if (!existBookmarks) iconExist else iconAdd
    e.presentation.text = if (!existBookmarks) textRemove else textAdd
  }
}
