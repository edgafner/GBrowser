package com.github.gbrowser.util

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.services.providers.CachingWebPageTitleLoader
import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBrowser
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentManager
import com.intellij.util.application
import java.util.concurrent.CompletableFuture

object GBrowserToolWindowUtil {
  private fun getFavIconLoader(): CachingWebPageTitleLoader = service()

  fun getSelectedBrowserPanel(anActionEvent: AnActionEvent): GBrowserToolWindowBrowser? {
    val project = anActionEvent.getData(CommonDataKeys.PROJECT) ?: return null
    return getSelectedBrowserPanel(project)
  }

  fun getSelectedBrowserPanel(project: Project): GBrowserToolWindowBrowser? {
    val toolWindow = getToolWindow(project, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID) ?: return null
    val selectedContent = toolWindow.contentManager.selectedContent ?: return null
    return selectedContent.component as? GBrowserToolWindowBrowser
  }


  fun createContentTab(toolWindow: ToolWindow, url: String, tabName: String) {
    val toolWindowPanel = GBrowserToolWindowBrowser(toolWindow)
    url.let { toolWindowPanel.loadUrl(it) }


    val titleFuture = if (tabName.isBlank()) { // Asynchronously fetch the title
      getFavIconLoader().getTitleOfWebPage(url)
    } else { // Immediately completed future with the existing tab name
      CompletableFuture.completedFuture(tabName)
    }


    titleFuture.thenAccept { title ->
      application.invokeLater {
        val content = toolWindow.contentManager.factory.createContent(toolWindowPanel, title, false).apply {
          preferredFocusableComponent = toolWindowPanel.component
        }
        content.component = toolWindowPanel
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)

        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, toolWindow.project.service<GBrowserService>().isTabIconVisible)
        content.icon = AllIcons.General.Web
      }
    }
  }


  fun createContentTab(event: AnActionEvent?, id: String, url: String) {
    getToolWindow(event?.project, id)?.let { toolWindow ->
      createContentTab(toolWindow, url, id)
    }
  }

  fun createContentTabAndShow(event: AnActionEvent?, id: String, url: String) {
    getToolWindow(event?.project, id)?.let { toolWindow ->
      createContentTab(toolWindow, url, id)
      application.invokeLater {
        toolWindow.show()
      }

    }
  }

  private fun getToolWindowManager(project: Project?): ToolWindowManager? {
    return project?.let { ToolWindowManager.getInstance(it) }
  }

  fun getToolWindow(project: Project?, id: String): ToolWindow? {
    val toolWindowManager = getToolWindowManager(project)
    return toolWindowManager?.getToolWindow(id)
  }

  fun getContentManager(project: Project?, id: String): ContentManager? {
    val toolWindowManager = getToolWindowManager(project)
    return toolWindowManager?.getToolWindow(id)?.contentManager
  }

  fun getAllBrowsers(project: Project): List<GCefBrowser> {
    val browsers = mutableListOf<GCefBrowser>()
    val toolWindow = getToolWindow(project, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)
    toolWindow?.contentManager?.contents?.forEach { content ->
      val panel = content.component as? GBrowserToolWindowBrowser
      panel?.let {
        browsers.add(it.getBrowser())
      }
    }
    return browsers
  }


}
