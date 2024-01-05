package com.github.gbrowser.actions.bookmark


import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBuilder
import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.github.gbrowser.util.GBrowserUtil
import com.github.gbrowser.util.GBrowserUtil.loadFavIcon
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import javax.swing.Icon


class GBrowserBookmarkGroupItemAction(val name: String, val url: String, initialIcon: Icon = AllIcons.General.Web) : AnAction({ name }) {

  private val settings: GBrowserSetting = GBrowserSetting.instance()

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
        val loadedIcon = loadFavIcon(url) ?: return
        updateIcon(loadedIcon)
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
    val panel = getSelectedBrowserPanel(anActionEvent)
    val isDefaultUrlLoaded = panel?.getCurrentUrl() == settings.defaultUrl

    if (isDefaultUrlLoaded) {
      panel?.loadUrl(url)
    } else {
      GBrowserToolWindowBuilder.createContentTab(anActionEvent, GBrowserUtil.GROUP_DISPLAY_ID, url)
    }
  }
}
