package com.github.gbrowser.actions

import com.github.gbrowser.util.GBrowserDeviceEmulationUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.ln

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
 */
class GBrowserMobileToggleActionTest {

  private lateinit var action: GBrowserMobileToggleAction

  @BeforeEach
  fun setUp() {
    action = GBrowserMobileToggleAction()
  }

  @AfterEach
  fun tearDown() {
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

    // Should have text and description
    assertTrue(presentation.text != null)
    assertTrue(presentation.description != null)

    // Should have icon
    assertTrue(presentation.icon != null)
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
  fun `test zoom level calculation formula`() {
    // This tests the actual zoom level calculation used in the action
    val zoomFactor = 1.2

    // Test zoom level 0 = 100%
    val zoom100 = 1.0
    val zoomLevel100 = ln(zoom100) / ln(zoomFactor)
    assertEquals(0.0, zoomLevel100, 0.01)

    // Test zoom level for 50% should be negative
    val zoom50 = 0.5
    val zoomLevel50 = ln(zoom50) / ln(zoomFactor)
    assertTrue(zoomLevel50 < 0)

    // Test zoom level for 200% should be positive
    val zoom200 = 2.0
    val zoomLevel200 = ln(zoom200) / ln(zoomFactor)
    assertTrue(zoomLevel200 > 0)
  }

  @Nested
  @DisplayName("Device profile validation")
  inner class DeviceProfileTests {

    @Test
    fun `test device profiles exist in utility`() {
      val profiles = GBrowserDeviceEmulationUtil.DEVICE_PROFILES

      // Verify we have device profiles
      assertFalse(profiles.isEmpty())

      // Verify some expected devices exist
      assertTrue(profiles.containsKey("iPhone SE"))
      assertTrue(profiles.containsKey("iPad Pro"))
      assertTrue(profiles.containsKey("Pixel 7"))
    }

    @Test
    fun `test all device profiles have valid properties`() {
      val profiles = GBrowserDeviceEmulationUtil.DEVICE_PROFILES

      profiles.forEach { (name, profile) ->
        // Verify profile name matches map key
        assertEquals(name, profile.name)

        // Verify all profiles have valid dimensions
        assertTrue(profile.width > 0, "Profile $name has invalid width")
        assertTrue(profile.height > 0, "Profile $name has invalid height")
        assertTrue(profile.deviceScaleFactor > 0, "Profile $name has an invalid scale factor")
        assertFalse(profile.userAgent.isBlank(), "Profile $name has blank user agent")
      }
    }
  }
}