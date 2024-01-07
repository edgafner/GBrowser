package com.github.gbrowser.actions.bookmark

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon


class GBrowserBookmarkAddAction : AnAction(), DumbAware {
  private val settings: GBrowserService = GBrowserService.instance()
  private var existBookmarks: Boolean = false
  private val iconAdd: Icon = GBrowserIcons.BOOKMARK_REMOVE
  private val iconExist: Icon = GBrowserIcons.BOOKMARK_ADD
  private val textAdd: String = GBrowserBundle.message("actions.bookmark.add.text")
  private val textRemove: String = GBrowserBundle.message("actions.bookmark.remove.text")


  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null || !panel.hasContent()) {
      e.presentation.isEnabled = false
      return
    }

    e.presentation.isEnabled = true
    val currentUrl = panel.getCurrentUrl()
    existBookmarks = settings.existBookmark(currentUrl)

    e.presentation.icon = if (existBookmarks) iconExist else iconAdd
    e.presentation.text = if (existBookmarks) textRemove else textAdd
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e) ?: return
    panel.let {
      val favorite = GBrowserBookmark(panel.getCurrentUrl(), panel.getCurrentTitle())
      if (existBookmarks) {
        settings.removeBookmark(favorite)
      } else {
        settings.addBookmarks(favorite)
      }
    }
    panel.updateUI()
  }
}
