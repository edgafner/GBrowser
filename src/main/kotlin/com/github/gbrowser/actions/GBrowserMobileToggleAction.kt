package com.github.gbrowser.actions

import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.util.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*
import kotlin.math.ln

@Suppress("DuplicatedCode")
class GBrowserMobileToggleAction : ToggleAction(
  GBrowserBundle.message("action.GBrowserMobileToggleAction.text"),
  GBrowserBundle.message("action.GBrowserMobileToggleAction.description"),
  com.github.gbrowser.GBrowserIcons.TOGGLE_DEVICES
), DumbAware {

  @Suppress("CompanionObjectInExtension")
  companion object {
    private val LOG = thisLogger()
    private val deviceEmulationState = mutableMapOf<String, DeviceEmulationState>()
    private val stateLock = Any() // Lock for synchronizing state access

    // Chrome DevTools colors - matching the actual Chrome DevTools colors
    private val CHROME_DEVTOOLS_DARK_BG = JBColor(Color(0x202124), Color(0x202124)) // Chrome's actual dark background (lighter gray)
    private val CHROME_DEVTOOLS_LIGHT_BG = JBColor(Color(0xF3F3F3), Color(0xF3F3F3)) // Chrome's actual light background

    // Default responsive mode dimensions
    private const val DEFAULT_RESPONSIVE_WIDTH = 400
    private const val DEFAULT_RESPONSIVE_HEIGHT = 626

    // Zoom calculation constant
    private const val ZOOM_FACTOR = 1.2

    /**
     * Cleans up device emulation state for a disposed browser.
     * Should be called when a browser is closed or disposed.
     */
    fun cleanupBrowserState(browserId: String) {
      synchronized(stateLock) {
        deviceEmulationState.remove(browserId)?.let { state ->
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

  private data class DeviceEmulationState(
    var isActive: Boolean = false,
    var currentDevice: String? = null,
    var deviceToolbar: JPanel? = null,
    var browserWrapper: JPanel? = null,
    var isRotated: Boolean = false,
    var currentWidth: Int = DEFAULT_RESPONSIVE_WIDTH,
    var currentHeight: Int = DEFAULT_RESPONSIVE_HEIGHT
  )

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun isSelected(e: AnActionEvent): Boolean {
    val project = e.project
    if (project == null) {
      LOG.debug("GBrowserMobileToggleAction: isSelected - no project")
      return false
    }

    val browser = getCurrentBrowser(project)
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
    val browser = getCurrentBrowser(project) ?: return

    LOG.info("GBrowserMobileToggleAction: setSelected called with state=$state")

    // Try to get the browser panel - handle both regular browser and DevTools
    val browserPanel = getBrowserPanel(project)

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
      enableDeviceEmulation(browserPanel, browser)
    } else {
      disableDeviceEmulation(browserPanel, browser)
    }
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    val project = e.project
    val browser = project?.let { getCurrentBrowser(it) }

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

  private fun enableDeviceEmulation(browserPanel: SimpleToolWindowPanel, browser: GCefBrowser) {
    val state = synchronized(stateLock) {
      val s = deviceEmulationState.getOrPut(browser.id) { DeviceEmulationState() }
      if (s.isActive) return
      s.isActive = true
      s
    }

    LOG.info("GBrowserMobileToggleAction: Enabling device emulation for browser ${browser.id}")

    // Create device toolbar
    state.deviceToolbar = createDeviceToolbar(browser)

    // Create browser wrapper with padding - use Chrome DevTools colors
    val project = browser.project
    val isDarkTheme = GBrowserThemeUtil.isDarkTheme(project)
    state.browserWrapper = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
      // Use Chrome DevTools colors based on theme
      background = if (isDarkTheme) {
        CHROME_DEVTOOLS_DARK_BG
      } else {
        CHROME_DEVTOOLS_LIGHT_BG
      }
      border = JBUI.Borders.empty(20)
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
    applyResponsiveMode(browser, state)

    browserPanel.revalidate()
    browserPanel.repaint()

    // Ensure device frame update happens after layout is complete
    // Use invokeAndWait to avoid race conditions
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeAndWait {
        LOG.info("GBrowserMobileToggleAction: Updating device frame after layout")
        updateDeviceFrame(browser, state)
      }
    } else {
      // If already on EDT, schedule for next cycle to ensure layout is complete
      SwingUtilities.invokeLater {
        LOG.info("GBrowserMobileToggleAction: Updating device frame after layout")
        updateDeviceFrame(browser, state)
      }
    }
  }

  private fun disableDeviceEmulation(browserPanel: SimpleToolWindowPanel, browser: GCefBrowser) {
    val state = synchronized(stateLock) {
      val s = deviceEmulationState[browser.id] ?: return
      if (!s.isActive) return
      s.isActive = false
      s
    }

    LOG.info("GBrowserMobileToggleAction: Disabling device emulation for browser ${browser.id}")

    // Clean up UI components properly before nulling references
    synchronized(stateLock) {
      // Remove browser from wrapper to prevent memory leaks
      state.browserWrapper?.removeAll()
      state.deviceToolbar?.removeAll()

      state.currentDevice = null
      state.deviceToolbar = null
      state.browserWrapper = null
      state.isRotated = false
      state.currentWidth = DEFAULT_RESPONSIVE_WIDTH
      state.currentHeight = DEFAULT_RESPONSIVE_HEIGHT
    }

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
  }

  private fun createDeviceToolbar(browser: GCefBrowser): JPanel {
    val state = synchronized(stateLock) {
      deviceEmulationState[browser.id] ?: throw IllegalStateException("Device emulation state not found for browser ${browser.id}")
    }

    val deviceComboBox = ComboBox<String>().apply {
      // Add "Responsive" as default
      addItem("Responsive")

      // Add all device profiles
      GBrowserDeviceEmulationUtil.DEVICE_PROFILES.keys.forEach { deviceName ->
        addItem(deviceName)
      }

      // Add action listener
      addActionListener { _ ->
        when (val selected = selectedItem as? String) {
          "Responsive" -> {
            LOG.info("GBrowserMobileToggleAction: Selected Responsive mode")
            applyResponsiveMode(browser, state)
          }
          null -> {
            LOG.info("GBrowserMobileToggleAction: No device selected")
          }
          else -> {
            LOG.info("GBrowserMobileToggleAction: Selected device: $selected")
            GBrowserDeviceEmulationUtil.DEVICE_PROFILES[selected]?.let { profile ->
              applyDeviceProfile(browser, profile, state)
            }
          }
        }
      }
    }

    // Width spinner
    val widthSpinner = JSpinner(SpinnerNumberModel(state.currentWidth, 50, 9999, 1)).apply {
      name = "widthSpinner"
      preferredSize = Dimension(80, preferredSize.height)
      addChangeListener {
        val newWidth = value as Int
        if (state.currentWidth != newWidth) {
          LOG.debug("GBrowserMobileToggleAction: Width changed from ${state.currentWidth} to $newWidth")
          state.currentWidth = newWidth
          updateDeviceSize(browser, state)
        }
      }
    }

    // Height spinner
    val heightSpinner = JSpinner(SpinnerNumberModel(state.currentHeight, 50, 9999, 1)).apply {
      name = "heightSpinner"
      preferredSize = Dimension(80, preferredSize.height)
      addChangeListener {
        val newHeight = value as Int
        if (state.currentHeight != newHeight) {
          LOG.debug("GBrowserMobileToggleAction: Height changed from ${state.currentHeight} to $newHeight")
          state.currentHeight = newHeight
          updateDeviceSize(browser, state)
        }
      }
    }

    // Zoom combo box
    val zoomComboBox = ComboBox(arrayOf("50%", "75%", "100%", "125%", "150%")).apply {
      selectedItem = "100%"
      isEditable = true
      preferredSize = Dimension(80, preferredSize.height)
      addActionListener {
        val zoomText = selectedItem as? String ?: return@addActionListener
        val zoomValue = zoomText.removeSuffix("%").toDoubleOrNull() ?: return@addActionListener
        val zoom = zoomValue / 100.0
        val zoomLevel = ln(zoom) / ln(ZOOM_FACTOR)
        LOG.debug("GBrowserMobileToggleAction: Zoom changed to $zoomText, zoom level: $zoomLevel")
        browser.cefBrowser.zoomLevel = zoomLevel
      }
    }


    // Rotate button
    val rotateButton = JButton(AllIcons.Actions.SyncPanels).apply {
      toolTipText = "Rotate"
      preferredSize = Dimension(30, 26)
      addActionListener {
        state.isRotated = !state.isRotated
        // Swap width and height
        val temp = state.currentWidth
        state.currentWidth = state.currentHeight
        state.currentHeight = temp

        LOG.debug("GBrowserMobileToggleAction: Rotated device - new dimensions: ${state.currentWidth}x${state.currentHeight}")

        // Update spinners
        widthSpinner.value = state.currentWidth
        heightSpinner.value = state.currentHeight

        // Update device
        updateDeviceSize(browser, state)
      }
    }

    return JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT, 5, 2)).apply {
      // Match Chrome DevTools toolbar styling
      val isDarkTheme = GBrowserThemeUtil.isDarkTheme(browser.project)
      background = if (isDarkTheme) {
        JBColor(Color(0x2B2D30), Color(0x2B2D30)) // Chrome DevTools toolbar dark background
      } else {
        JBColor(Color(0xF3F3F3), Color(0xF3F3F3)) // Chrome DevTools toolbar light background  
      }
      border = JBUI.Borders.compound(
        JBUI.Borders.customLine(
          if (isDarkTheme) JBColor(Color(0x393B3F), Color(0x393B3F)) else JBColor(Color(0xD0D0D0), Color(0xD0D0D0)),
          0, 0, 1, 0
        ),
        JBUI.Borders.empty(5, 10)
      )

      // Device selector
      add(deviceComboBox)

      // Size controls
      add(Box.createHorizontalStrut(10))
      add(widthSpinner)
      add(JBLabel("×").apply {
        border = JBUI.Borders.empty(0, 3)
      })
      add(heightSpinner)

      // Zoom controls
      add(Box.createHorizontalStrut(10))
      add(zoomComboBox)
      add(JBLabel("%"))

      // Action buttons
      add(Box.createHorizontalStrut(10))
      add(rotateButton)
    }
  }

  private fun applyResponsiveMode(browser: GCefBrowser, state: DeviceEmulationState) {
    state.currentDevice = "Responsive"

    // Set default responsive size
    if (state.currentWidth == 0 || state.currentHeight == 0) {
      state.currentWidth = 400
      state.currentHeight = 626
    }

    LOG.info("GBrowserMobileToggleAction: Applying responsive mode with dimensions ${state.currentWidth}x${state.currentHeight}")

    // Update the device frame with responsive dimensions
    updateDeviceFrame(browser, state)

    // Update spinners
    updateSpinnersFromState(state)

    // Reset any device-specific emulation
    GBrowserDeviceEmulationUtil.resetDeviceEmulation(browser.cefBrowser)

    // Apply responsive mode with custom dimensions
    val responsiveProfile = DeviceProfile(
      name = "Responsive",
      width = state.currentWidth,
      height = state.currentHeight,
      deviceScaleFactor = 1.0,
      userAgent = browser.cefBrowser.url?.let {
        // Use a mobile user agent for responsive mode
        "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
      } ?: "",
      isMobile = true,
      hasTouch = true
    )

    GBrowserDeviceEmulationUtil.applyDeviceEmulation(browser.cefBrowser, responsiveProfile)

    state.browserWrapper?.revalidate()
    state.browserWrapper?.repaint()
  }

  private fun applyDeviceProfile(browser: GCefBrowser, profile: DeviceProfile, state: DeviceEmulationState) {
    state.currentDevice = profile.name
    state.currentWidth = profile.width
    state.currentHeight = profile.height
    state.isRotated = false

    LOG.info("GBrowserMobileToggleAction: Applying device profile '${profile.name}' with dimensions ${profile.width}x${profile.height}")

    // Update browser wrapper to show device frame first
    updateDeviceFrame(browser, state)

    // Update spinners
    updateSpinnersFromState(state)

    // Apply JavaScript emulation after frame is set
    GBrowserDeviceEmulationUtil.applyDeviceEmulation(browser.cefBrowser, profile)

    // Reload the page to apply all changes
    browser.cefBrowser.reload()

    state.browserWrapper?.revalidate()
    state.browserWrapper?.repaint()
  }

  private fun getCurrentBrowser(project: Project): GCefBrowser? {
    LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - looking for browser in the project")

    // First try to get from the regular browser window
    val browserPanel = GBrowserToolWindowUtil.getSelectedBrowserPanel(project)
    if (browserPanel != null) {
      val browser = browserPanel.getBrowser()
      LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - found browser in the main panel: ${browser.id}")
      return browser
    }

    // If not found, try to get from DevTools window
    val toolWindowManager = project.getService(com.intellij.openapi.wm.ToolWindowManager::class.java)
    val devToolsWindow = toolWindowManager.getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID)
    val selectedContent = devToolsWindow?.contentManager?.selectedContent
    val devToolsPanel = selectedContent?.component as? com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevTools

    val devBrowser = devToolsPanel?.browser
    LOG.debug("GBrowserMobileToggleAction: getCurrentBrowser - devtools browser: ${devBrowser?.id}")
    return devBrowser
  }

  private fun getBrowserPanel(project: Project): SimpleToolWindowPanel? {
    LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - looking for the browser panel")

    // Get the browser tool window panel directly - it should be a SimpleToolWindowPanel
    val browserPanel = GBrowserToolWindowUtil.getSelectedBrowserPanel(project)
    if (browserPanel is SimpleToolWindowPanel) {
      LOG.debug("GBrowserMobileToggleAction: getBrowserPanel - found the main browser panel")
      return browserPanel
    }

    // If not found, try to get from DevTools window
    val toolWindowManager = project.getService(com.intellij.openapi.wm.ToolWindowManager::class.java)
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

  private fun updateSpinnersFromState(state: DeviceEmulationState) {
    state.deviceToolbar?.components?.forEach { component ->
      if (component is JSpinner) {
        when (component.name) {
          "widthSpinner" -> {
            LOG.debug("GBrowserMobileToggleAction: Setting width spinner to ${state.currentWidth}")
            component.value = state.currentWidth
          }
          "heightSpinner" -> {
            LOG.debug("GBrowserMobileToggleAction: Setting height spinner to ${state.currentHeight}")
            component.value = state.currentHeight
          }
        }
      }
    }
  }

  private fun updateDeviceSize(browser: GCefBrowser, state: DeviceEmulationState) {
    LOG.debug("GBrowserMobileToggleAction: updateDeviceSize - ${state.currentWidth}x${state.currentHeight}")

    // Create custom device profile with current dimensions
    val customProfile = DeviceProfile(
      name = "Custom",
      width = state.currentWidth,
      height = state.currentHeight,
      deviceScaleFactor = 1.0,
      userAgent = browser.cefBrowser.url?.let {
        "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
      } ?: ""
    )

    // Apply the custom profile
    GBrowserDeviceEmulationUtil.applyDeviceEmulation(browser.cefBrowser, customProfile)

    // Update the frame
    updateDeviceFrame(browser, state)
  }

  private fun updateDeviceFrame(browser: GCefBrowser, state: DeviceEmulationState) {
    state.browserWrapper?.let { wrapper ->
      wrapper.removeAll()

      // Get available space in the wrapper (accounting for padding)
      val wrapperWidth = wrapper.width
      val wrapperHeight = wrapper.height

      // If wrapper dimensions are not yet set, use the device dimensions as-is
      if (wrapperWidth <= 40 || wrapperHeight <= 40) {
        LOG.info("GBrowserMobileToggleAction: Wrapper dimensions not ready ($wrapperWidth x $wrapperHeight), using device dimensions as-is")
        updateDeviceFrameWithDimensions(browser, state, state.currentWidth, state.currentHeight, 1.0)
        return
      }

      val availableWidth = wrapperWidth - 40 // 20px padding on each side
      val availableHeight = wrapperHeight - 40 // 20px padding on top/bottom

      // Calculate scale if the device is too large
      var deviceWidth = state.currentWidth
      var deviceHeight = state.currentHeight
      var scale = 1.0

      if (deviceWidth > availableWidth || deviceHeight > availableHeight) {
        val scaleX = availableWidth.toDouble() / deviceWidth
        val scaleY = availableHeight.toDouble() / deviceHeight
        scale = minOf(scaleX, scaleY, 1.0) // Never scale up, only down

        // Apply scale with some margin
        scale *= 0.9 // 90% to ensure some padding
        deviceWidth = (state.currentWidth * scale).toInt()
        deviceHeight = (state.currentHeight * scale).toInt()
      }

      LOG.info("GBrowserMobileToggleAction: Updating device frame - Original: ${state.currentWidth}x${state.currentHeight}, Scaled: ${deviceWidth}x${deviceHeight} (scale: $scale)")

      updateDeviceFrameWithDimensions(browser, state, deviceWidth, deviceHeight, scale)
    }
  }

  private fun updateDeviceFrameWithDimensions(browser: GCefBrowser, state: DeviceEmulationState, deviceWidth: Int, deviceHeight: Int, scale: Double) {
    state.browserWrapper?.let { wrapper ->
      // Get project from browser
      val project = browser.project

      // Create device frame panel
      val deviceFrame = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        preferredSize = Dimension(deviceWidth, deviceHeight)
        minimumSize = preferredSize
        maximumSize = preferredSize

        // Match Chrome DevTools styling
        val isDarkTheme = GBrowserThemeUtil.isDarkTheme(project)
        background = if (isDarkTheme) {
          JBColor(Color(0x292A2D), Color(0x292A2D)) // Slightly lighter than the wrapper for contrast
        } else {
          JBColor(Color(0xFFFFFF), Color(0xFFFFFF)) // White for light theme
        }
        border = BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(
            if (isDarkTheme) JBColor(Color(0x3C3F41), Color(0x3C3F41)) else JBColor(Color(0xD0D0D0), Color(0xD0D0D0)),
            1
          ),
          BorderFactory.createEmptyBorder(2, 2, 2, 2) // Small inner padding
        )

        // Ensure browser component respects the scaled device frame size
        browser.component.preferredSize = Dimension(deviceWidth, deviceHeight)
        browser.component.minimumSize = Dimension(deviceWidth, deviceHeight)
        browser.component.maximumSize = Dimension(deviceWidth, deviceHeight)

        // Add the browser component
        add(browser.component, BorderLayout.CENTER)
      }

      val constraints = GridBagConstraints().apply {
        gridx = 0
        gridy = 0
        anchor = GridBagConstraints.CENTER
      }

      wrapper.add(deviceFrame, constraints)
      wrapper.revalidate()
      wrapper.repaint()

      // Force browser to recognize the new size
      browser.component.revalidate()
      // Use original dimensions for the browser viewport, not scaled dimensions
      browser.cefBrowser.wasResized(state.currentWidth, state.currentHeight)

      // Apply zoom if scaled
      if (scale < 1.0) {
        val zoomLevel = ln(scale) / ln(1.2)
        browser.cefBrowser.zoomLevel = zoomLevel
        LOG.info("GBrowserMobileToggleAction: Applied zoom level $zoomLevel for scale $scale")
      }

      LOG.info("GBrowserMobileToggleAction: Device frame updated and browser resized")
    }
  }
}