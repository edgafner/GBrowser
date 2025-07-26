package com.github.gbrowser.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserMobileToggleAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components and JCEF browser
 * instances, this test focuses on the basic properties of the action that can be tested
 * without extensive mocking.
 *
 * Full integration testing of device emulation functionality would require:
 * - A running IntelliJ instance with JCEF support
 * - Actual browser instances
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action thread safety
 * - Basic action properties
 */
class GBrowserMobileToggleActionTest {

  private lateinit var action: GBrowserMobileToggleAction

  @BeforeEach
  fun setUp() {
    action = GBrowserMobileToggleAction()
  }

  @Test
  fun `test action is thread safe and runs on EDT`() {
    // Device emulation UI changes must happen on EDT
    assertEquals(ActionUpdateThread.EDT, action.getActionUpdateThread())
  }

  @Test
  fun `test action has correct template presentation`() {
    val presentation = action.templatePresentation

    // Should have text and description from the bundle
    assert(presentation.text?.isNotEmpty() == true)
    assert(presentation.description?.isNotEmpty() == true)

    // Should have icon
    assert(presentation.icon != null)
  }
}