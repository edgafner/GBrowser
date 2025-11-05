package com.github.gbrowser.actions

import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel

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

    // DevTools tool window removed - no longer available in new API (253 EAP)
    LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - no browser found")
    return null
  }

  fun getBrowserPanel(project: Project): SimpleToolWindowPanel? {
    LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - looking for the browser panel")

    // Get the browser tool window panel directly - it should be a SimpleToolWindowPanel
    val browserPanel = GBrowserToolWindowUtil.getSelectedBrowserPanel(project)
    if (browserPanel is SimpleToolWindowPanel) {
      LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - found the main browser panel")
      return browserPanel
    }

    // DevTools tool window removed - no longer available in new API (253 EAP)

    LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - no panel found")
    return null
  }
}