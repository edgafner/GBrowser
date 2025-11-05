package com.github.gbrowser.actions.browser

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserFindAction focusing on basic properties and thread safety.
 *
 * These tests verify the basic configuration and thread safety aspects of the action
 * without requiring complex IntelliJ platform integration.
 */
class GBrowserFindActionTest {

  private lateinit var action: GBrowserFindAction

  @BeforeEach
  fun setUp() {
    action = GBrowserFindAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action runs on EDT thread`() {
    // Find action should run on EDT for UI updates
    assertEquals(ActionUpdateThread.EDT, action.actionUpdateThread)
  }

  @Test
  fun `test action is not null after instantiation`() {
    // Basic smoke test to ensure the action instantiates correctly
    assertNotNull(action)
    assertNotNull(action.actionUpdateThread)
  }
  
}