package com.github.gbrowser.actions.bookmark

import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit tests for GBrowserBookmarkAddAction.
 *
 * Note: Due to the complexity of mocking IntelliJ Platform components, browser panels,
 * and service dependencies, this test focuses on the basic properties of the action that
 * can be tested without extensive mocking.
 *
 * Full integration testing would require:
 * - A running IntelliJ instance
 * - Project with GBrowserService initialized
 * - Browser panel with content loaded
 * - Tool window system initialized
 * - UI testing framework
 *
 * The following aspects are tested:
 * - Action instantiation
 * - Thread safety (EDT execution for UI updates)
 * - Basic action properties
 * - DumbAware implementation
 * - Icon and text properties initialization
 */
@DisplayName("GBrowserBookmarkAddAction Tests")
class GBrowserBookmarkAddActionTest {

  private lateinit var action: GBrowserBookmarkAddAction

  @BeforeEach
  fun setUp() {
    action = GBrowserBookmarkAddAction()
  }

  @Test
  fun `test action can be instantiated`() {
    assertNotNull(action)
  }

  @Test
  fun `test action is thread safe and runs on EDT`() {
    // Bookmark actions run on EDT as they update UI presentation and interact with services
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
