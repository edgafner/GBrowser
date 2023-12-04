package com.github.gbrowser.actions

import com.github.gbrowser.services.GBrowserSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBImageIcon
import java.net.URL
import javax.imageio.ImageIO

private val LOG = logger<GBookMarksMenuAction>()

class GBookMarksMenuAction(val jbCefBrowser: JBCefBrowser) : DefaultActionGroup(), DumbAware {

  init {
    updateView()
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = childrenCount <= 0
    e.presentation.icon = AllIcons.Nodes.Favorite

  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  internal fun updateView() {
    LOG.info("Update View was invoked")
    removeAll()
    GBrowserSettings.instance().getBookmarks().forEach { bookmarks ->
      try {
        val imageIo = JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=${bookmarks.webUrl}")))
        add(GBrowserBookmarksAction(bookmarks.webUrl, imageIo, jbCefBrowser))
      }
      catch (e: Exception) { // The image is not persisted anyway, so we can just ignore it
        LOG.warn("Failed to load favicon for ${bookmarks.webUrl}")
        add(GBrowserBookmarksAction(bookmarks.webUrl, AllIcons.General.Web, jbCefBrowser))
        LOG.warn("Added default icon")
      }
    }
  }
}