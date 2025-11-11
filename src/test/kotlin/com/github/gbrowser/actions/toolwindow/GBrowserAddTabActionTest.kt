package com.github.gbrowser.actions.toolwindow

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserAddTabAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components, tool windows,
 * and content managers, this test focuses on the basic properties of the action that
 * can be tested without extensive mocking.
 *
 * Full integration testing would require:
 * - A running IntelliJ instance
 * - Tool window system initialized
 * - Service components available
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety (EDT execution for UI updates)
 * - Modal context support
 * - Basic action properties
 * - DumbAware implementation
 */
@DisplayName("GBrowserAddTabAction Tests")
class GBrowserAddTabActionTest {

  private lateinit var action: GBrowserAddTabAction

  @BeforeEach
  fun setUp() {
    action = GBrowserAddTabAction()
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
    // GBrowserAddTabAction sets isEnabledInModalContext = true in init block
    assertTrue(action.isEnabledInModalContext)
  }

  @Test
  fun `test action has template presentation with text`() {
    val presentation = action.templatePresentation
    assertNotNull(presentation)
    // Template text is set via plugin.xml
    assertNotNull(presentation.text)
  }

  @Test
  fun `test action has template presentation with description`() {
    val presentation = action.templatePresentation
    // Template description is set via plugin.xml
    assertNotNull(presentation.description)
  }
}
