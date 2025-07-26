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
    assertEquals(DeviceEmulationState.DEFAULT_RESPONSIVE_WIDTH, state.currentWidth)
    assertEquals(DeviceEmulationState.DEFAULT_RESPONSIVE_HEIGHT, state.currentHeight)
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
    assertEquals(400, DeviceEmulationState.DEFAULT_RESPONSIVE_WIDTH)
    assertEquals(626, DeviceEmulationState.DEFAULT_RESPONSIVE_HEIGHT)
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

    state.currentWidth = DeviceEmulationState.DEFAULT_RESPONSIVE_WIDTH
    state.currentHeight = DeviceEmulationState.DEFAULT_RESPONSIVE_HEIGHT
    state.currentDevice = null

    assertEquals(DeviceEmulationState.DEFAULT_RESPONSIVE_WIDTH, state.currentWidth)
    assertEquals(DeviceEmulationState.DEFAULT_RESPONSIVE_HEIGHT, state.currentHeight)
    assertNull(state.currentDevice)
  }
}