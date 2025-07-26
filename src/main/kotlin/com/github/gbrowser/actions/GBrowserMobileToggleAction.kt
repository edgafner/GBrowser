package com.github.gbrowser.actions

import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import java.util.*

class GBrowserMobileToggleAction : ToggleAction(
  GBrowserBundle.message("action.GBrowserMobileToggleAction.text"),
  GBrowserBundle.message("action.GBrowserMobileToggleAction.description"),
  com.github.gbrowser.GBrowserIcons.TOGGLE_DEVICES
), DumbAware {

  /**
   * Companion object for GBrowserMobileToggleAction.
   * @Suppress("CompanionObjectInExtension") is used because we need static state management
   * for device emulation across multiple browser instances.
   */
  @Suppress("CompanionObjectInExtension")
  companion object {
    private val LOG = thisLogger()

    // Use WeakHashMap to prevent memory leaks - entries are automatically removed
    // when the browser ID (key) is no longer strongly referenced elsewhere
    private val deviceEmulationState = Collections.synchronizedMap(WeakHashMap<String, DeviceEmulationState>())
    private val stateLock = Any() // Lock for synchronizing state access
    

    /**
     * Cleans up device emulation state for a disposed browser.
     * Should be called when a browser is closed or disposed.
     */
    fun cleanupBrowserState(browserId: String) {
      synchronized(stateLock) {
        deviceEmulationState.remove(browserId)?.also { state ->
          LOG.debug("GBrowserMobileToggleAction: Cleaned up device emulation state for browser $browserId")
          // Clean up any UI components
          state.browserWrapper?.removeAll()
          state.deviceToolbar?.removeAll()
          state.deviceToolbar = null
          state.browserWrapper = null
        }
      }
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun isSelected(e: AnActionEvent): Boolean {
    val project = e.project
    if (project == null) {
      LOG.debug("GBrowserMobileToggleAction: isSelected - no project")
      return false
    }

    val browser = GBrowserMobileToggleActionUtils.getCurrentBrowser(project)
    if (browser == null) {
      LOG.debug("GBrowserMobileToggleAction: isSelected - no browser found")
      return false
    }

    val isActive = synchronized(stateLock) {
      deviceEmulationState[browser.id]?.isActive ?: false
    }
    LOG.debug("GBrowserMobileToggleAction: isSelected - browser ${browser.id}, active: $isActive")
    return isActive
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val project = e.project ?: return
    val browser = GBrowserMobileToggleActionUtils.getCurrentBrowser(project) ?: return

    LOG.info("GBrowserMobileToggleAction: setSelected called with state=$state")

    // Try to get the browser panel - handle both regular browser and DevTools
    val browserPanel = GBrowserMobileToggleActionUtils.getBrowserPanel(project)

    // If we couldn't find the browser panel directly, it might be in DevTools
    if (browserPanel == null) {
      // For DevTools, we need to handle it differently
      val toolWindowManager = project.getService(com.intellij.openapi.wm.ToolWindowManager::class.java)
      val devToolsWindow = toolWindowManager.getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID)
      val selectedContent = devToolsWindow?.contentManager?.selectedContent
      val devToolsPanel = selectedContent?.component as? com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevTools

      if (devToolsPanel != null) {
        // For DevTools, we need to create a wrapper approach
        // This is a limitation - device emulation works best in the main browser window
        com.intellij.notification.NotificationGroupManager.getInstance()
          .getNotificationGroup("GBrowser Notifications")
          .createNotification(
            "Emulation device",
            "Device emulation works best in the main browser window. Please use the browser tab instead of DevTools.",
            com.intellij.notification.NotificationType.WARNING
          )
          .notify(project)
        return
      }
    }

    if (browserPanel == null) {
      com.intellij.notification.NotificationGroupManager.getInstance()
        .getNotificationGroup("GBrowser Notifications")
        .createNotification(
          "Emulation device error",
          "Could not find the browser panel. Please open a browser tab first.",
          com.intellij.notification.NotificationType.ERROR
        )
        .notify(project)
      return
    }

    if (state) {
      val emulationState = synchronized(stateLock) {
        val s = deviceEmulationState.getOrPut(browser.id) { DeviceEmulationState() }
        if (s.isActive) return
        s.isActive = true
        s
      }
      GBrowserMobileToggleActionHelper.enableDeviceEmulation(browserPanel, browser, emulationState)
    } else {
      val emulationState = synchronized(stateLock) {
        val s = deviceEmulationState[browser.id] ?: return
        if (!s.isActive) return
        s.isActive = false
        s
      }
      GBrowserMobileToggleActionHelper.disableDeviceEmulation(browserPanel, browser, emulationState) {
        synchronized(stateLock) {
          deviceEmulationState[browser.id] = emulationState
        }
      }
    }
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    val project = e.project
    val browser = project?.let { GBrowserMobileToggleActionUtils.getCurrentBrowser(it) }

    LOG.debug("GBrowserMobileToggleAction: update - project: ${project != null}, browser: ${browser?.id}")

    e.presentation.isEnabled = browser != null
    e.presentation.icon = com.github.gbrowser.GBrowserIcons.TOGGLE_DEVICES

    if (browser != null) {
      val (isActive, currentDevice) = synchronized(stateLock) {
        val state = deviceEmulationState[browser.id]
        (state?.isActive == true) to state?.currentDevice
      }
      val text = if (isActive && currentDevice != null) {
        GBrowserBundle.message("action.device.emulation.active", currentDevice)
      } else {
        GBrowserBundle.message("emulate.mobile.description")
      }
      e.presentation.text = text
      LOG.debug("GBrowserMobileToggleAction: update - text: $text, state active: $isActive, device: $currentDevice")
    }
  }
}