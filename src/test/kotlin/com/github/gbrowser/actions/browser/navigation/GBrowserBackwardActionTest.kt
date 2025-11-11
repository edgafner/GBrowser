package com.github.gbrowser.actions.browser.navigation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserBackwardAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components and browser panels,
 * this test focuses on the basic properties of the action that can be tested without
 * extensive mocking.
 *
 * Full integration testing would require:
 * - A running IntelliJ instance
 * - Actual browser instances with navigation history
 * - Tool window system initialized
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety (BGT execution)
 * - Basic action properties
 * - DumbAware implementation
 */
@DisplayName("GBrowserBackwardAction Tests")
class GBrowserBackwardActionTest {

  private lateinit var action: GBrowserBackwardAction

  @BeforeEach
  fun setUp() {
    action = GBrowserBackwardAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action is thread safe and runs on BGT`() {
    // Navigation actions should run on Background Thread (BGT) for performance
    assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
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
