package com.github.gbrowser.actions

import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBrowser
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GBrowserMobileToggleActionUtilsTest {

  private lateinit var mockProject: Project
  private lateinit var mockToolWindowManager: ToolWindowManager
  private lateinit var mockToolWindow: ToolWindow
  private lateinit var mockBrowserPanel: GBrowserToolWindowBrowser
  private lateinit var mockBrowser: GCefBrowser

  @BeforeEach
  fun setUp() {
    mockProject = mockk(relaxed = true)
    mockToolWindowManager = mockk(relaxed = true)
    mockToolWindow = mockk(relaxed = true)
    mockBrowserPanel = mockk(relaxed = true)
    mockBrowser = mockk(relaxed = true)

    every { mockProject.getService(ToolWindowManager::class.java) } returns mockToolWindowManager
    every { mockBrowserPanel.getBrowser() } returns mockBrowser
    every { mockBrowser.id } returns "test-browser-1"

    mockkStatic(GBrowserToolWindowUtil::class)
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `test getCurrentBrowser returns browser from main panel`() {
    // Given
    every { GBrowserToolWindowUtil.getSelectedBrowserPanel(mockProject) } returns mockBrowserPanel

    // When
    val result = GBrowserMobileToggleActionUtils.getCurrentBrowser(mockProject)

    // Then
    assertEquals(mockBrowser, result)
    verify { GBrowserToolWindowUtil.getSelectedBrowserPanel(mockProject) }
  }

  @Test
  fun `test getCurrentBrowser returns null when no browser found`() {
    // Given
    every { GBrowserToolWindowUtil.getSelectedBrowserPanel(mockProject) } returns null
    // DevTools tool window removed - no longer available in new API (253 EAP)

    // When
    val result = GBrowserMobileToggleActionUtils.getCurrentBrowser(mockProject)

    // Then
    assertNull(result)
  }

  @Test
  fun `test getBrowserPanel returns SimpleToolWindowPanel from main browser`() {
    // Given
    // GBrowserToolWindowBrowser extends SimpleToolWindowPanel
    every { GBrowserToolWindowUtil.getSelectedBrowserPanel(mockProject) } returns mockBrowserPanel

    // When
    val result = GBrowserMobileToggleActionUtils.getBrowserPanel(mockProject)

    // Then,
    // Since GBrowserToolWindowBrowser is a SimpleToolWindowPanel, it should be returned
    assertEquals(mockBrowserPanel, result)
  }

  @Test
  fun `test getBrowserPanel returns null when no panel found`() {
    // Given
    every { GBrowserToolWindowUtil.getSelectedBrowserPanel(mockProject) } returns null
    // DevTools tool window removed - no longer available in new API (253 EAP)

    // When
    val result = GBrowserMobileToggleActionUtils.getBrowserPanel(mockProject)

    // Then
    assertNull(result)
  }
}