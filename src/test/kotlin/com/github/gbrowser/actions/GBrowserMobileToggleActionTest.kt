package com.github.gbrowser.actions

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
    mockkObject(GBrowserToolWindowUtil)
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
    // Clean up any lingering state
    GBrowserMobileToggleAction.cleanupBrowserState("test-browser-1")
    GBrowserMobileToggleAction.cleanupBrowserState("test-browser-2")
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

  @Test
  fun `test cleanup browser state removes state safely`() {
    val browserId = "test-browser-cleanup"

    // Test cleanup without complex mocking
    // First, ensure no state exists
    GBrowserMobileToggleAction.cleanupBrowserState(browserId)

    // Cleanup should be safe to call multiple times
    assertDoesNotThrow {
      GBrowserMobileToggleAction.cleanupBrowserState(browserId)
      GBrowserMobileToggleAction.cleanupBrowserState(browserId)
    }
  }

  @Test
  fun `test concurrent cleanup is thread safe`() {
    val iterations = 50
    val latch = CountDownLatch(iterations * 2)
    val errors = mutableListOf<Exception>()

    // Create threads that concurrently add and remove states
    val threads = (1..iterations).flatMap { i ->
      val browserId = "browser-$i"
      listOf(
        Thread {
          try {
            // Simulate state cleanup
            GBrowserMobileToggleAction.cleanupBrowserState(browserId)
            latch.countDown()
          } catch (e: Exception) {
            errors.add(e)
            latch.countDown()
          }
        },
        Thread {
          try {
            // Simulate another cleanup on same browser
            GBrowserMobileToggleAction.cleanupBrowserState(browserId)
            latch.countDown()
          } catch (e: Exception) {
            errors.add(e)
            latch.countDown()
          }
        }
      )
    }

    // Start all threads
    threads.forEach { it.start() }

    // Wait for completion
    assertTrue(latch.await(5, TimeUnit.SECONDS), "Threads did not complete in time")

    // Verify no exceptions occurred
    assertTrue(errors.isEmpty(), "Concurrent access caused exceptions: $errors")
  }

  @Test
  fun `test multiple browsers can be cleaned up independently`() {
    val browserIds = (1..10).map { "browser-$it" }

    // Clean up all browsers
    assertDoesNotThrow {
      browserIds.forEach { browserId ->
        GBrowserMobileToggleAction.cleanupBrowserState(browserId)
      }
    }

    // Clean up again should be safe
    assertDoesNotThrow {
      browserIds.forEach { browserId ->
        GBrowserMobileToggleAction.cleanupBrowserState(browserId)
      }
    }
  }
}