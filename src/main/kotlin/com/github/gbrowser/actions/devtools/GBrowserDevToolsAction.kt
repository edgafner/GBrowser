package com.github.gbrowser.actions.devtools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevToolsFactory
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import javax.swing.Icon

class GBrowserDevToolsAction : AnAction() {
  companion object {
    private val LOG = logger<GBrowserDevToolsAction>()
  }

  private val icon: Icon = GBrowserIcons.DEV_TOOLS_ACTIVE
  private val iconActive: Icon = GBrowserIcons.DEV_TOOLS

  override fun update(e: AnActionEvent) {
    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      e.presentation.isEnabled = false
      e.presentation.icon = icon
      return
    }

    e.presentation.isEnabled = panel.hasContent()
    e.presentation.icon = iconActive
    e.presentation.text = "Open DevTools"
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    LOG.debug("GBrowserDevToolsAction.actionPerformed() called")

    val project = e.getData(CommonDataKeys.PROJECT)
    if (project == null) {
      LOG.debug("Project is null, returning")
      return
    }

    val panel = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)
    if (panel == null) {
      LOG.debug("No browser panel selected, returning")
      return
    }

    // Check if the panel has loaded content
    if (!panel.hasContent()) {
      LOG.debug("Panel has no content, returning")
      return
    }

    try {
      LOG.debug("Opening DevTools for browser panel")

      // Check settings for display mode
      val settings = project.service<com.github.gbrowser.services.GBrowserService>()
      val openInDialog = settings.isDevToolsInDialog
      LOG.debug("isDevToolsInDialog setting: $openInDialog")

      if (openInDialog) {
        // Open in dialog using JCEF's built-in DevTools dialog
        LOG.debug("Opening DevTools in dialog")
        val browser = panel.getBrowser()
        val devToolsBrowser = browser.cefBrowser.devTools
        val frame = javax.swing.JFrame("DevTools - ${browser.cefBrowser.url}")
        frame.defaultCloseOperation = javax.swing.WindowConstants.DISPOSE_ON_CLOSE
        frame.add(devToolsBrowser.uiComponent)
        frame.setSize(1024, 768)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
      } else {
        // Get the DevTools browser instance
        val devToolsBrowser = panel.getDevToolsBrowser()
        LOG.debug("Got DevTools browser instance: $devToolsBrowser")

        // Open in the tool window
        val tabName = panel.getCurrentTitle()
        LOG.debug("Opening DevTools in the tool window with title: $tabName")
        GBrowserToolWindowDevToolsFactory.Companion.createTab(project, devToolsBrowser, tabName)
      }
      LOG.debug("DevTools opened successfully")
    } catch (ex: Exception) {
      LOG.warn("Failed to open DevTools: ${ex.message}", ex)
    }
  }
}