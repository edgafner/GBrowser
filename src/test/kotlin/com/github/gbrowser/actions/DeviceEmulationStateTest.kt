package com.github.gbrowser.actions

import com.intellij.ui.components.JBPanel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.swing.JPanel

class DeviceEmulationStateTest {

  @Test
  fun `test default state values`() {
    val state = DeviceEmulationState()

    assertFalse(state.isActive)
    assertNull(state.currentDevice)
    assertNull(state.deviceToolbar)
    assertNull(state.browserWrapper)
    assertFalse(state.isRotated)
    assertEquals(DeviceEmulationConstants.DEFAULT_RESPONSIVE_WIDTH, state.currentWidth)
    assertEquals(DeviceEmulationConstants.DEFAULT_RESPONSIVE_HEIGHT, state.currentHeight)
  }

  @Test
  fun `test state with custom values`() {
    val toolbar = JPanel()
    val wrapper = JBPanel<JBPanel<*>>()

    val state = DeviceEmulationState(
      isActive = true,
      currentDevice = "iPhone 12",
      deviceToolbar = toolbar,
      browserWrapper = wrapper,
      isRotated = true,
      currentWidth = 800,
      currentHeight = 600
    )

    assertTrue(state.isActive)
    assertEquals("iPhone 12", state.currentDevice)
    assertEquals(toolbar, state.deviceToolbar)
    assertEquals(wrapper, state.browserWrapper)
    assertTrue(state.isRotated)
    assertEquals(800, state.currentWidth)
    assertEquals(600, state.currentHeight)
  }

  @Test
  fun `test state mutation`() {
    val state = DeviceEmulationState()

    state.isActive = true
    state.currentDevice = "iPad Pro"
    state.currentWidth = 1024
    state.currentHeight = 768

    assertEquals("iPad Pro", state.currentDevice)
    assertEquals(1024, state.currentWidth)
    assertEquals(768, state.currentHeight)
  }

  @Test
  fun `test default responsive dimensions`() {
    assertEquals(400, DeviceEmulationConstants.DEFAULT_RESPONSIVE_WIDTH)
    assertEquals(626, DeviceEmulationConstants.DEFAULT_RESPONSIVE_HEIGHT)
  }

  @Test
  fun `test state handles extreme dimensions`() {
    val state = DeviceEmulationState(
      currentWidth = 1,
      currentHeight = 9999
    )

    assertEquals(1, state.currentWidth)
    assertEquals(9999, state.currentHeight)
  }

  @Suppress("KotlinConstantConditions")
  @Test
  fun `test state cleans up correctly`() {
    val toolbar = JPanel()
    val wrapper = JBPanel<JBPanel<*>>()

    val state = DeviceEmulationState(
      isActive = true,
      deviceToolbar = toolbar,
      browserWrapper = wrapper
    )

    // Simulate cleanup
    state.deviceToolbar = null
    state.browserWrapper = null
    state.isActive = false

    assertNull(state.deviceToolbar)
    assertNull(state.browserWrapper)
    assertFalse(state.isActive)
  }

  @Test
  fun `test state preserves default device values when reset`() {
    val state = DeviceEmulationState(
      currentWidth = 800,
      currentHeight = 600,
      currentDevice = "Custom"
    )

    state.currentWidth = DeviceEmulationConstants.DEFAULT_RESPONSIVE_WIDTH
    state.currentHeight = DeviceEmulationConstants.DEFAULT_RESPONSIVE_HEIGHT
    state.currentDevice = null

    assertEquals(DeviceEmulationConstants.DEFAULT_RESPONSIVE_WIDTH, state.currentWidth)
    assertEquals(DeviceEmulationConstants.DEFAULT_RESPONSIVE_HEIGHT, state.currentHeight)
    assertNull(state.currentDevice)
  }

  @Test
  fun `test updated user agent constants contain Chrome 131`() {
    // Test that updated mobile user agent contains Chrome 131
    assertTrue(DeviceEmulationConstants.MOBILE_USER_AGENT_ANDROID.contains("Chrome/140.0.7339.133"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_PIXEL_7.contains("Chrome/140.0.7339.133"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_SAMSUNG_S20_ULTRA.contains("Chrome/140.0.7339.133"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_GALAXY_Z_FOLD_5.contains("Chrome/140.0.7339.133"))
  }

  @Test
  fun `test iOS user agents contain iOS 17`() {
    // Test that iOS devices have been updated to iOS 17
    assertTrue(DeviceEmulationConstants.USER_AGENT_IPHONE_SE.contains("CPU iPhone OS 17_0"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_IPHONE_XR.contains("CPU iPhone OS 17_0"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_IPHONE_12_PRO.contains("CPU iPhone OS 17_0"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_IPAD_MINI.contains("CPU OS 17_0"))
  }

  @Test
  fun `test modern browser user agent for anti-detection`() {
    // Test new modern user agent constant
    val modernUserAgent = DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER
    assertTrue(modernUserAgent.contains("Chrome/140.0.7339.133"))
    assertTrue(modernUserAgent.contains("Windows NT 10.0"))
    assertFalse(modernUserAgent.contains("CefSharp"))
    assertFalse(modernUserAgent.contains("/CefSharp Browser"))
  }

  @Test
  fun `test default browser user agent maintains Gmail compatibility`() {
    // Test that default user agent still contains Gmail-compatible identifiers
    val defaultUserAgent = DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER
    assertTrue(defaultUserAgent.contains("Chrome/140.0.7339.133"))
    assertTrue(defaultUserAgent.contains("/CefSharp Browser 90.0"))
  }

  @Test
  fun `test Android versions updated to modern releases`() {
    // Test Android versions have been updated
    assertTrue(DeviceEmulationConstants.MOBILE_USER_AGENT_ANDROID.contains("Android 14"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_PIXEL_7.contains("Android 14"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_SAMSUNG_S20_ULTRA.contains("Android 14"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_GALAXY_Z_FOLD_5.contains("Android 14"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_SAMSUNG_A51_71.contains("Android 13"))
    assertTrue(DeviceEmulationConstants.USER_AGENT_SURFACE_DUO.contains("Android 12"))
  }
}