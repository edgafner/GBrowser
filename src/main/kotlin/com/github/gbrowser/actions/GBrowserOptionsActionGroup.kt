package com.github.gbrowser.actions

import com.github.gbrowser.services.GBrowserSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBImageIcon
import java.net.URL
import javax.imageio.ImageIO


class GBrowserOptionsActionGroup(private val jbCefBrowser: JBCefBrowser) : ActionGroup("Options", "Options",
                                                                                       AllIcons.Actions.More), DumbAware {


  private val zoomInButton = GZoomInAction(jbCefBrowser, AllIcons.General.ZoomIn)
  private val zoomOutButton = GZoomOutAction(jbCefBrowser, AllIcons.General.ZoomOut)
  private val findButton = GFindAction(jbCefBrowser, AllIcons.Actions.Find)

  init {
    isPopup = true
    isSearchable = false
    templatePresentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true)

    findButton.registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts(IdeActions.ACTION_FIND), jbCefBrowser.component)

  }


  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val actions = mutableListOf<AnAction>()

    actions.add(zoomInButton)
    actions.add(zoomOutButton)
    actions.add(findButton)
    actions.add(Separator.getInstance())

    val quickAccessActions = GBrowserSettings.instance().getQuickAccessBookmarks().map { bookmarks ->
      try {
        val imageIo = JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=${bookmarks.webUrl}")))
        GBrowserBookmarksAction(bookmarks.webUrl, imageIo, jbCefBrowser)
      }
      catch (e: Exception) { // The image is not persisted anyway, so we can just ignore it
        GBrowserBookmarksAction(bookmarks.webUrl, AllIcons.General.Web, jbCefBrowser)
      }
    }

    // Add all quick access actions to the main actions list
    actions.addAll(quickAccessActions)

    // Convert the MutableList to Array and return
    return actions.toTypedArray()
  }


}

