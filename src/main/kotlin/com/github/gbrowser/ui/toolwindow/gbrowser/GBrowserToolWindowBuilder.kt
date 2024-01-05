package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindow

object GBrowserToolWindowBuilder {


  fun createContentTab(toolWindow: ToolWindow, url: String, tabName: String) {
    val toolWindowPanel = GBrowserToolWindowBrowser(toolWindow)
    url.let { toolWindowPanel.loadUrl(it) }

    val title = tabName.ifBlank {
      GBrowserUtil.getTitleOfWebPage(url)
    }


    val content = toolWindow.contentManager.factory.createContent(toolWindowPanel, title, false).apply {
      preferredFocusableComponent = toolWindowPanel.component
    }

    content.component = toolWindowPanel
    toolWindow.contentManager.addContent(content)
    toolWindow.contentManager.setSelectedContent(content)

    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, GBrowserSetting.instance().isTabIconVisible)
    content.icon = AllIcons.General.Web
  }


  fun createContentTab(event: AnActionEvent?, id: String, url: String) {
    getToolWindow(event?.project, id)?.let { toolWindow ->
      createContentTab(toolWindow, url, id)
    }
  }


}
