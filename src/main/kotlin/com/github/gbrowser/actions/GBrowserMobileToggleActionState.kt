package com.github.gbrowser.actions

import com.intellij.ui.components.JBPanel
import javax.swing.JPanel

/**
 * Represents the state of device emulation for a browser.
 */
data class DeviceEmulationState(
  var isActive: Boolean = false,
  var currentDevice: String? = null,
  var deviceToolbar: JPanel? = null,
  var browserWrapper: JBPanel<JBPanel<*>>? = null,
  var isRotated: Boolean = false,
  var currentWidth: Int = DeviceEmulationConstants.DEFAULT_RESPONSIVE_WIDTH,
  var currentHeight: Int = DeviceEmulationConstants.DEFAULT_RESPONSIVE_HEIGHT
) 