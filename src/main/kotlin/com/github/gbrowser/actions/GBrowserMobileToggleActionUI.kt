package com.github.gbrowser.actions

import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.util.DeviceProfile
import com.github.gbrowser.util.GBrowserDeviceEmulationUtil
import com.github.gbrowser.util.GBrowserThemeUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*
import kotlin.math.ln

object GBrowserMobileToggleActionUI {
  private val LOG = thisLogger()

  fun createDeviceToolbar(browser: GCefBrowser, state: DeviceEmulationState): JPanel {
    val deviceComboBox = createDeviceComboBox(browser, state)
    val widthSpinner = createWidthSpinner(state, browser)
    val heightSpinner = createHeightSpinner(state, browser)
    val zoomComboBox = createZoomComboBox(browser)
    val rotateButton = createRotateButton(browser, state, widthSpinner, heightSpinner)

    return JBPanel<JBPanel<*>>(FlowLayout(FlowLayout.LEFT, DeviceEmulationConstants.TOOLBAR_HORIZONTAL_GAP, DeviceEmulationConstants.TOOLBAR_VERTICAL_GAP)).apply {
      // Match Chrome DevTools toolbar styling
      val isDarkTheme = GBrowserThemeUtil.isDarkTheme(browser.project)
      background = if (isDarkTheme) {
        JBColor(Color(DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_DARK_BG), Color(DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_DARK_BG))
      } else {
        JBColor(Color(DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_LIGHT_BG), Color(DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_LIGHT_BG))
      }
      border = JBUI.Borders.compound(
        JBUI.Borders.customLine(
          if (isDarkTheme) JBColor(Color(DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BORDER), Color(DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BORDER)) else JBColor(
            Color(
              DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BORDER
            ), Color(DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BORDER)
          ),
          0, 0, 1, 0
        ),
        JBUI.Borders.empty(DeviceEmulationConstants.TOOLBAR_PADDING, DeviceEmulationConstants.TOOLBAR_HORIZONTAL_PADDING)
      )

      // Device selector
      add(deviceComboBox)

      // Size controls
      add(Box.createHorizontalStrut(DeviceEmulationConstants.HORIZONTAL_STRUT_SIZE))
      add(widthSpinner)
      add(JBLabel("×").apply {
        border = JBUI.Borders.empty(0, DeviceEmulationConstants.LABEL_HORIZONTAL_PADDING)
      })
      add(heightSpinner)

      // Zoom controls
      add(Box.createHorizontalStrut(DeviceEmulationConstants.HORIZONTAL_STRUT_SIZE))
      add(zoomComboBox)
      add(JBLabel("%"))

      // Action buttons
      add(Box.createHorizontalStrut(DeviceEmulationConstants.HORIZONTAL_STRUT_SIZE))
      add(rotateButton)
    }
  }

  private fun createDeviceComboBox(browser: GCefBrowser, state: DeviceEmulationState): ComboBox<String> {
    return ComboBox<String>().apply {
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
  }

  private fun createWidthSpinner(state: DeviceEmulationState, browser: GCefBrowser): JSpinner {
    return JSpinner(
      SpinnerNumberModel(
        state.currentWidth,
        DeviceEmulationConstants.MIN_DEVICE_DIMENSION,
        DeviceEmulationConstants.MAX_DEVICE_DIMENSION,
        DeviceEmulationConstants.DIMENSION_STEP
      )
    ).apply {
      name = DeviceEmulationConstants.DEVICE_WIDTH_SPINNER_NAME
      preferredSize = Dimension(DeviceEmulationConstants.SPINNER_WIDTH, preferredSize.height)
      addChangeListener {
        val newWidth = value as Int
        if (state.currentWidth != newWidth) {
          LOG.debug("GBrowserMobileToggleAction: Width changed from ${state.currentWidth} to $newWidth")
          state.currentWidth = newWidth
          updateDeviceSize(browser, state)
        }
      }
    }
  }

  private fun createHeightSpinner(state: DeviceEmulationState, browser: GCefBrowser): JSpinner {
    return JSpinner(
      SpinnerNumberModel(
        state.currentHeight,
        DeviceEmulationConstants.MIN_DEVICE_DIMENSION,
        DeviceEmulationConstants.MAX_DEVICE_DIMENSION,
        DeviceEmulationConstants.DIMENSION_STEP
      )
    ).apply {
      name = DeviceEmulationConstants.DEVICE_HEIGHT_SPINNER_NAME
      preferredSize = Dimension(DeviceEmulationConstants.SPINNER_WIDTH, preferredSize.height)
      addChangeListener {
        val newHeight = value as Int
        if (state.currentHeight != newHeight) {
          LOG.debug("GBrowserMobileToggleAction: Height changed from ${state.currentHeight} to $newHeight")
          state.currentHeight = newHeight
          updateDeviceSize(browser, state)
        }
      }
    }
  }

  private fun createZoomComboBox(browser: GCefBrowser): ComboBox<String> {
    return ComboBox(arrayOf("50%", "75%", "100%", "125%", "150%")).apply {
      selectedItem = DeviceEmulationConstants.DEFAULT_ZOOM_PERCENTAGE
      isEditable = true
      preferredSize = Dimension(DeviceEmulationConstants.SPINNER_WIDTH, preferredSize.height)
      addActionListener {
        val zoomText = selectedItem as? String ?: return@addActionListener
        val zoomValue = zoomText.removeSuffix("%").toDoubleOrNull() ?: return@addActionListener
        val zoom = zoomValue / 100.0
        val zoomLevel = ln(zoom) / ln(DeviceEmulationConstants.ZOOM_FACTOR)
        LOG.debug("GBrowserMobileToggleAction: Zoom changed to $zoomText, zoom level: $zoomLevel")
        browser.cefBrowser.zoomLevel = zoomLevel
      }
    }
  }

  private fun createRotateButton(
    browser: GCefBrowser,
    state: DeviceEmulationState,
    widthSpinner: JSpinner,
    heightSpinner: JSpinner
  ): JButton {
    return JButton(AllIcons.Actions.SyncPanels).apply {
      toolTipText = "Rotate"
      preferredSize = Dimension(DeviceEmulationConstants.ROTATE_BUTTON_WIDTH, DeviceEmulationConstants.ROTATE_BUTTON_HEIGHT)
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
  }

  fun applyResponsiveMode(browser: GCefBrowser, state: DeviceEmulationState) {
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
        DeviceEmulationConstants.MOBILE_USER_AGENT_ANDROID
      } ?: "",
      isMobile = true,
      hasTouch = true
    )

    GBrowserDeviceEmulationUtil.applyDeviceEmulation(browser.cefBrowser, responsiveProfile)

    state.browserWrapper?.revalidate()
    state.browserWrapper?.repaint()
  }

  fun applyDeviceProfile(browser: GCefBrowser, profile: DeviceProfile, state: DeviceEmulationState) {
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

  fun updateDeviceFrame(browser: GCefBrowser, state: DeviceEmulationState) {
    val wrapper = state.browserWrapper ?: return
    wrapper.removeAll()

    // Get available space in the wrapper (accounting for padding)
    val wrapperWidth = wrapper.width
    val wrapperHeight = wrapper.height

    // If wrapper dimensions are not yet set, use the device dimensions as-is
    if (wrapperWidth <= DeviceEmulationConstants.DEVICE_FRAME_PADDING || wrapperHeight <= DeviceEmulationConstants.DEVICE_FRAME_PADDING) {
      LOG.info("GBrowserMobileToggleAction: Wrapper dimensions not ready ($wrapperWidth x $wrapperHeight), using device dimensions as-is")
      updateDeviceFrameWithDimensions(browser, state, state.currentWidth, state.currentHeight, 1.0)
      return
    }

    val availableWidth = wrapperWidth - DeviceEmulationConstants.DEVICE_FRAME_PADDING
    val availableHeight = wrapperHeight - DeviceEmulationConstants.DEVICE_FRAME_PADDING

    // Calculate scale if the device is too large
    var deviceWidth = state.currentWidth
    var deviceHeight = state.currentHeight
    var scale = 1.0

    if (deviceWidth > availableWidth || deviceHeight > availableHeight) {
      val scaleX = availableWidth.toDouble() / deviceWidth
      val scaleY = availableHeight.toDouble() / deviceHeight
      scale = minOf(scaleX, scaleY, 1.0) // Never scale up, only down

      // Apply scale with some margin
      scale *= DeviceEmulationConstants.SCALE_PADDING_FACTOR
      deviceWidth = (state.currentWidth * scale).toInt()
      deviceHeight = (state.currentHeight * scale).toInt()
    }

    LOG.info("GBrowserMobileToggleAction: Updating device frame - Original: ${state.currentWidth}x${state.currentHeight}, Scaled: ${deviceWidth}x${deviceHeight} (scale: $scale)")

    updateDeviceFrameWithDimensions(browser, state, deviceWidth, deviceHeight, scale)
  }

  fun updateSpinnersFromState(state: DeviceEmulationState) {
    state.deviceToolbar?.components?.forEach { component ->
      if (component is JSpinner) {
        when (component.name) {
          DeviceEmulationConstants.DEVICE_WIDTH_SPINNER_NAME -> {
            LOG.debug("GBrowserMobileToggleAction: Setting width spinner to ${state.currentWidth}")
            component.value = state.currentWidth
          }
          DeviceEmulationConstants.DEVICE_HEIGHT_SPINNER_NAME -> {
            LOG.debug("GBrowserMobileToggleAction: Setting height spinner to ${state.currentHeight}")
            component.value = state.currentHeight
          }
        }
      }
    }
  }

  fun updateDeviceSize(browser: GCefBrowser, state: DeviceEmulationState) {
    LOG.debug("GBrowserMobileToggleAction: updateDeviceSize - ${state.currentWidth}x${state.currentHeight}")

    // Create custom device profile with current dimensions
    val customProfile = DeviceProfile(
      name = "Custom",
      width = state.currentWidth,
      height = state.currentHeight,
      deviceScaleFactor = 1.0,
      userAgent = browser.cefBrowser.url?.let {
        DeviceEmulationConstants.MOBILE_USER_AGENT_ANDROID
      } ?: ""
    )

    // Apply the custom profile
    GBrowserDeviceEmulationUtil.applyDeviceEmulation(browser.cefBrowser, customProfile)

    // Update the frame
    updateDeviceFrame(browser, state)
  }

  private fun updateDeviceFrameWithDimensions(browser: GCefBrowser, state: DeviceEmulationState, deviceWidth: Int, deviceHeight: Int, scale: Double) {
    val wrapper = state.browserWrapper ?: return
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
        JBColor(Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BG), Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BG))
      } else {
        JBColor(Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BG), Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BG))
      }
      border = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(
          if (isDarkTheme) JBColor(Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BORDER), Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BORDER)) else JBColor(
            Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BORDER),
            Color(DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BORDER)
          ),
          1
        ),
        BorderFactory.createEmptyBorder(
          DeviceEmulationConstants.DEVICE_FRAME_INNER_PADDING,
          DeviceEmulationConstants.DEVICE_FRAME_INNER_PADDING,
          DeviceEmulationConstants.DEVICE_FRAME_INNER_PADDING,
          DeviceEmulationConstants.DEVICE_FRAME_INNER_PADDING
        )
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