package com.github.gib.actions

import com.github.gib.services.GivServiceSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBImageIcon
import java.net.URL
import javax.imageio.ImageIO

class GFavoritesMenuAction(val jbCefBrowser: JBCefBrowser) : DefaultActionGroup(), DumbAware {

  init {
    updateView()
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = childrenCount <= 0
    e.presentation.icon = AllIcons.Nodes.Favorite

  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  fun updateView() {
    removeAll()
    GivServiceSettings.instance().getFavorites().forEach {
      val imageIo = JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=${it.webUrl}")))
      add(GFavoriteWebAction(it.webUrl, imageIo, jbCefBrowser))
    }
  }

}




