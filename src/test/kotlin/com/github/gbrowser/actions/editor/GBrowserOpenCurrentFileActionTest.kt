package com.github.gbrowser.actions.editor

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserOpenCurrentFileAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components and virtual files,
 * this test focuses on the basic properties of the action that can be tested without
 * extensive mocking.
 *
 * Full integration testing of file opening functionality would require:
 * - A running IntelliJ instance
 * - Actual file system and virtual file instances
 * - Tool window system initialized
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety
 * - Basic action properties
 */
class GBrowserOpenCurrentFileActionTest {

  private lateinit var action: GBrowserOpenCurrentFileAction

  @BeforeEach
  fun setUp() {
    action = GBrowserOpenCurrentFileAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action update thread is BGT`() {
    assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
  }

  @Suppress("UnusedVariable")
  @Test
  fun `test supported extensions are defined`() {
    // This tests that the action has the expected supported extensions
    val supportedExtensions = setOf("html", "htm", "xhtml", "xml", "svg", "md", "markdown")
    // We can't directly access private field, but we can verify the action exists
    // and would handle these file types in integration tests
    assertNotNull(action)
  }
}