package com.github.gbrowser.actions

import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.util.GBrowserDeviceEmulationUtil
import com.github.gbrowser.util.GBrowserThemeUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagLayout
import javax.swing.SwingUtilities

object GBrowserMobileToggleActionHelper {
  private val LOG = thisLogger()

  fun enableDeviceEmulation(
    browserPanel: SimpleToolWindowPanel,
    browser: GCefBrowser,
    state: DeviceEmulationState
  ) {
    LOG.info("GBrowserMobileToggleAction: Enabling device emulation for browser ${browser.id}")

    // Create device toolbar
    state.deviceToolbar = GBrowserMobileToggleActionUI.createDeviceToolbar(browser, state)

    // Create browser wrapper with padding - use Chrome DevTools colors
    val project = browser.project
    val isDarkTheme = GBrowserThemeUtil.isDarkTheme(project)
    state.browserWrapper = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
      // Use Chrome DevTools colors based on theme
      background = if (isDarkTheme) {
        JBColor(Color(DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BG), Color(DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BG))
      } else {
        JBColor(Color(DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BG), Color(DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BG))
      }
      border = JBUI.Borders.empty(DeviceEmulationConstants.DEVICE_FRAME_PADDING_HALF)
    }

    // Create a container panel
    val deviceToolbar = state.deviceToolbar ?: return
    val browserWrapper = state.browserWrapper ?: return

    val containerPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
      add(deviceToolbar, BorderLayout.NORTH)
      add(browserWrapper, BorderLayout.CENTER)
    }

    // Replace the content
    browserPanel.setContent(containerPanel)

    // Apply initial responsive mode
    GBrowserMobileToggleActionUI.applyResponsiveMode(browser, state)

    browserPanel.revalidate()
    browserPanel.repaint()

    // Ensure device frame update happens after layout is complete
    // Always use invokeLater to avoid potential deadlocks and ensure consistent behavior
    // This guarantees the update runs after the current EDT event completes
    SwingUtilities.invokeLater {
      commonUpdate(browser, state)
    }
  }

  private fun commonUpdate(browser: GCefBrowser, state: DeviceEmulationState) {
    LOG.debug("GBrowserMobileToggleAction: Updating device frame after layout")
    GBrowserMobileToggleActionUI.updateDeviceFrame(browser, state)
  }

  fun disableDeviceEmulation(
    browserPanel: SimpleToolWindowPanel,
    browser: GCefBrowser,
    state: DeviceEmulationState,
    onCleanup: () -> Unit
  ) {
    LOG.info("GBrowserMobileToggleAction: Disabling device emulation for browser ${browser.id}")

    // Clean up UI components properly before nulling references
    // Remove browser from wrapper to prevent memory leaks
    state.browserWrapper?.also { it.removeAll() }
    state.deviceToolbar?.also { it.removeAll() }

    state.currentDevice = null
    state.deviceToolbar = null
    state.browserWrapper = null
    state.isRotated = false
    state.currentWidth = DeviceEmulationState.DEFAULT_RESPONSIVE_WIDTH
    state.currentHeight = DeviceEmulationState.DEFAULT_RESPONSIVE_HEIGHT

    // Reset browser emulation
    GBrowserDeviceEmulationUtil.resetDeviceEmulation(browser.cefBrowser)

    // Reset zoom level to 100%
    browser.cefBrowser.zoomLevel = 0.0

    // Remove browser component from wrapper first to avoid duplicate parent issues
    state.browserWrapper?.remove(browser.component)

    // Restore original browser component
    browserPanel.setContent(browser.component)

    browserPanel.revalidate()
    browserPanel.repaint()

    // Force a reload to ensure all changes are applied
    browser.cefBrowser.reload()

    // Call cleanup callback
    onCleanup()
  }
}