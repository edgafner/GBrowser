package com.github.gbrowser.actions.toolwindow

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserCloseTabAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components, tool windows,
 * and content managers, this test focuses on the basic properties of the action that
 * can be tested without extensive mocking.
 *
 * Full integration testing would require:
 * - A running IntelliJ instance
 * - Tool window system initialized
 * - Content manager with active content
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety (EDT execution for UI updates)
 * - Modal context support
 * - Basic action properties
 * - DumbAware implementation
 */
@DisplayName("GBrowserCloseTabAction Tests")
class GBrowserCloseTabActionTest {

  private lateinit var action: GBrowserCloseTabAction

  @BeforeEach
  fun setUp() {
    action = GBrowserCloseTabAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action is thread safe and runs on EDT`() {
    // Tab management actions run on EDT as they modify UI state
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
  fun `test action is enabled in modal context`() {
    // GBrowserCloseTabAction sets isEnabledInModalContext = true in init block
    assertTrue(action.isEnabledInModalContext)
  }

  @Test
  fun `test action has template presentation`() {
    val presentation = action.templatePresentation
    assertNotNull(presentation)
  }
}
