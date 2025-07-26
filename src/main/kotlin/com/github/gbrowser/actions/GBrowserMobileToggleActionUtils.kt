package com.github.gbrowser.actions

import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindowManager

object GBrowserMobileToggleActionUtils {
  private val LOG = thisLogger()

  fun getCurrentBrowser(project: Project): GCefBrowser? {
    LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - looking for browser in the project")

    // First try to get from the regular browser window
    val browserPanel = GBrowserToolWindowUtil.getSelectedBrowserPanel(project)
    if (browserPanel != null) {
      val browser = browserPanel.getBrowser()
      LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - found browser in the main panel: ${browser.id}")
      return browser
    }

    // If not found, try to get from DevTools window
    val toolWindowManager = project.getService(ToolWindowManager::class.java)
    val devToolsWindow = toolWindowManager.getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID)
    val selectedContent = devToolsWindow?.contentManager?.selectedContent
    val devToolsPanel = selectedContent?.component as? com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevTools

    val devBrowser = devToolsPanel?.browser
    LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - devtools browser: ${devBrowser?.id}")
    return devBrowser
  }

  fun getBrowserPanel(project: Project): SimpleToolWindowPanel? {
    LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - looking for the browser panel")

    // Get the browser tool window panel directly - it should be a SimpleToolWindowPanel
    val browserPanel = GBrowserToolWindowUtil.getSelectedBrowserPanel(project)
    if (browserPanel is SimpleToolWindowPanel) {
      LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - found the main browser panel")
      return browserPanel
    }

    // If not found, try to get from DevTools window
    val toolWindowManager = project.getService(ToolWindowManager::class.java)
    val devToolsWindow = toolWindowManager.getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID)
    val selectedContent = devToolsWindow?.contentManager?.selectedContent
    val devToolsPanel = selectedContent?.component

    if (devToolsPanel is SimpleToolWindowPanel) {
      LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - found devtools panel")
      return devToolsPanel
    }

    LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - no panel found")
    return null
  }
}