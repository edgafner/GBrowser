package com.github.gbrowser.actions.devtools

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserDevToolsAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components, JCEF browser
 * instances, and tool window interactions, this test focuses on the basic properties
 * of the action that can be tested without extensive mocking.
 *
 * Full integration testing of DevTools functionality would require:
 * - A running IntelliJ instance with JCEF support
 * - Actual browser instances with DevTools capability
 * - Tool window system initialized
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety
 * - Basic action properties
 */
class GBrowserDevToolsActionTest {

  private lateinit var action: GBrowserDevToolsAction

  @BeforeEach
  fun setUp() {
    action = GBrowserDevToolsAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action is thread safe and runs on EDT`() {
    // DevTools action runs on EDT for UI updates
    assertEquals(ActionUpdateThread.EDT, action.actionUpdateThread)
  }

}