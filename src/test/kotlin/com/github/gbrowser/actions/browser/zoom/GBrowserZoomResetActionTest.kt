package com.github.gbrowser.actions.browser.zoom

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserZoomResetAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components and JCEF browser
 * instances, this test focuses on the basic properties of the action that can be tested
 * without extensive mocking.
 *
 * Full integration testing would require:
 * - A running IntelliJ instance with JCEF support
 * - Actual browser instances with content loaded
 * - Tool window system initialized
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety (EDT execution for UI updates)
 * - Basic action properties
 * - DumbAware implementation
 */
@DisplayName("GBrowserZoomResetAction Tests")
class GBrowserZoomResetActionTest {

  private lateinit var action: GBrowserZoomResetAction

  @BeforeEach
  fun setUp() {
    action = GBrowserZoomResetAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action is thread safe and runs on EDT`() {
    // Zoom actions run on EDT as they trigger UI updates
    assertEquals(ActionUpdateThread.EDT, action.actionUpdateThread)
  }

  @Test
  fun `test action implements DumbAware`() {
    // DumbAware allows action to work during indexing
    assertTrue(action is com.intellij.openapi.project.DumbAware)
  }

  @Test
  fun `test action extends AnAction`() {
    assertTrue(action is com.intellij.openapi.actionSystem.AnAction)
  }

  @Test
  fun `test action has template presentation`() {
    val presentation = action.templatePresentation
    assertNotNull(presentation)
  }
}
