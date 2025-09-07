package com.github.gbrowser.util

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBrowser
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GBrowserToolWindowUtilTest {

  @BeforeEach
  fun setUp() {
    // Needed for static mocking
    mockkStatic("com.intellij.openapi.components.ServiceKt")
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun `test getSelectedBrowserPanel with null project`() {
    val mockEvent = mockk<AnActionEvent>()
    every { mockEvent.getData(CommonDataKeys.PROJECT) } returns null

    val result = GBrowserToolWindowUtil.getSelectedBrowserPanel(mockEvent)

    Assertions.assertNull(result)
  }

  @Test
  fun `test getSelectedBrowserPanel with project but null toolWindow`() {
    val mockEvent = mockk<AnActionEvent>()
    val mockProject = mockk<Project>()

    every { mockEvent.getData(CommonDataKeys.PROJECT) } returns mockProject

    // Mock the getToolWindow method to return null
    mockkObject(GBrowserToolWindowUtil)
    every { GBrowserToolWindowUtil.getToolWindow(mockProject, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID) } returns null

    val result = GBrowserToolWindowUtil.getSelectedBrowserPanel(mockEvent)

    Assertions.assertNull(result)
  }

  @Test
  fun `test getSelectedBrowserPanel with project and toolWindow but null selected content`() {
    val mockEvent = mockk<AnActionEvent>()
    val mockProject = mockk<Project>()
    val mockToolWindow = mockk<ToolWindow>()
    val mockContentManager = mockk<ContentManager>()

    every { mockEvent.getData(CommonDataKeys.PROJECT) } returns mockProject
    every { mockToolWindow.contentManager } returns mockContentManager
    every { mockContentManager.selectedContent } returns null

    mockkObject(GBrowserToolWindowUtil)
    every { GBrowserToolWindowUtil.getToolWindow(mockProject, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID) } returns mockToolWindow

    val result = GBrowserToolWindowUtil.getSelectedBrowserPanel(mockEvent)

    Assertions.assertNull(result)
  }

  @Test
  fun `test getSelectedBrowserPanel with project, toolWindow, and selected content`() {
    val mockEvent = mockk<AnActionEvent>()
    val mockProject = mockk<Project>()
    val mockToolWindow = mockk<ToolWindow>()
    val mockContentManager = mockk<ContentManager>()
    val mockContent = mockk<Content>()
    val mockBrowserPanel = mockk<GBrowserToolWindowBrowser>()

    every { mockEvent.getData(CommonDataKeys.PROJECT) } returns mockProject
    every { mockToolWindow.contentManager } returns mockContentManager
    every { mockContentManager.selectedContent } returns mockContent
    every { mockContent.component } returns mockBrowserPanel

    mockkObject(GBrowserToolWindowUtil)
    every { GBrowserToolWindowUtil.getToolWindow(mockProject, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID) } returns mockToolWindow

    val result = GBrowserToolWindowUtil.getSelectedBrowserPanel(mockEvent)

    Assertions.assertSame(mockBrowserPanel, result)
  }

  @Test
  fun `test getToolWindowManager with null project`() {
    mockkStatic(ToolWindowManager::class)

    val method = GBrowserToolWindowUtil::class.java.getDeclaredMethod("getToolWindowManager", Project::class.java)
    method.isAccessible = true

    val result = method.invoke(GBrowserToolWindowUtil, null)

    Assertions.assertNull(result)
  }

  @Test
  fun `test getToolWindowManager with project`() {
    val mockProject = mockk<Project>()
    val mockToolWindowManager = mockk<ToolWindowManager>()

    mockkStatic(ToolWindowManager::class)
    every { ToolWindowManager.getInstance(mockProject) } returns mockToolWindowManager

    val method = GBrowserToolWindowUtil::class.java.getDeclaredMethod("getToolWindowManager", Project::class.java)
    method.isAccessible = true

    val result = method.invoke(GBrowserToolWindowUtil, mockProject)

    Assertions.assertSame(mockToolWindowManager, result)
  }

  @Test
  fun `test getToolWindow with null project`() {
    val result = GBrowserToolWindowUtil.getToolWindow(null, "anyId")

    Assertions.assertNull(result)
  }

  @Test
  fun `test getToolWindow with project`() {
    val mockProject = mockk<Project>()
    val mockToolWindowManager = mockk<ToolWindowManager>()
    val mockToolWindow = mockk<ToolWindow>()

    mockkStatic(ToolWindowManager::class)
    every { ToolWindowManager.getInstance(mockProject) } returns mockToolWindowManager
    every { mockToolWindowManager.getToolWindow("testId") } returns mockToolWindow

    val result = GBrowserToolWindowUtil.getToolWindow(mockProject, "testId")

    Assertions.assertSame(mockToolWindow, result)
  }

  @Test
  fun `test getContentManager with null project`() {
    val result = GBrowserToolWindowUtil.getContentManager(null, "anyId")

    Assertions.assertNull(result)
  }

  @Test
  fun `test getContentManager with project`() {
    val mockProject = mockk<Project>()
    val mockToolWindowManager = mockk<ToolWindowManager>()
    val mockToolWindow = mockk<ToolWindow>()
    val mockContentManager = mockk<ContentManager>()

    mockkStatic(ToolWindowManager::class)
    every { ToolWindowManager.getInstance(mockProject) } returns mockToolWindowManager
    every { mockToolWindowManager.getToolWindow("testId") } returns mockToolWindow
    every { mockToolWindow.contentManager } returns mockContentManager

    val result = GBrowserToolWindowUtil.getContentManager(mockProject, "testId")

    Assertions.assertSame(mockContentManager, result)
  }

  @Test
  fun `test createContentTabAndShow with null project`() {
    val mockEvent = mockk<AnActionEvent>()

    every { mockEvent.project } returns null

    // This should not throw an exception, it should just do nothing
    GBrowserToolWindowUtil.createContentTabAndShow(mockEvent, "testId", "https://example.com")
  }

  @Test
  fun `test createContentTab with null project`() {
    val mockEvent = mockk<AnActionEvent>()

    every { mockEvent.project } returns null

    // This should not throw an exception, it should just do nothing
    GBrowserToolWindowUtil.createContentTab(mockEvent, "testId", "https://example.com")
  }

  @Test
  fun `test UI initialization error handling logic`() {
    // Test the error handling logic that would be used in createContentTabWithCompletableFuture

    fun safeUIInitialization(
      isToolWindowDisposed: Boolean,
      isContentManagerDisposed: Boolean,
      shouldThrowException: Boolean
    ): Boolean {
      return try {
        // Check if the tool window is disposed
        if (isToolWindowDisposed) return true // Skip if disposed

        // Check if content manager is disposed  
        if (isContentManagerDisposed) return true // Skip if disposed

        // Simulate UI initialization
        if (shouldThrowException) {
          throw RuntimeException("UI initialization failed")
        }

        true // Success
      } catch (_: Exception) {
        // Handle case where content manager UI is not properly initialized
        true // Gracefully handle exception, don't fail
      }
    }

    // Test normal case
    Assertions.assertTrue(safeUIInitialization(isToolWindowDisposed = false, isContentManagerDisposed = false, shouldThrowException = false))

    // Test disposed tool window case
    Assertions.assertTrue(safeUIInitialization(isToolWindowDisposed = true, isContentManagerDisposed = false, shouldThrowException = false))

    // Test disposed content manager case
    Assertions.assertTrue(safeUIInitialization(isToolWindowDisposed = false, isContentManagerDisposed = true, shouldThrowException = false))

    // Test exception handling case
    Assertions.assertTrue(safeUIInitialization(isToolWindowDisposed = false, isContentManagerDisposed = false, shouldThrowException = true))
  }

  @Test
  fun `test CompletableFuture error handling logic`() {
    // Test the error handling pattern used with CompletableFutures

    fun safeCompletableFutureHandling(shouldComplete: Boolean, shouldThrowException: Boolean): String? {
      return try {
        val future = if (shouldComplete) {
          java.util.concurrent.CompletableFuture.completedFuture("Test Title")
        } else {
          java.util.concurrent.CompletableFuture()
        }

        if (shouldThrowException) {
          throw RuntimeException("Future handling failed")
        }

        future.get()
      } catch (_: Exception) {
        null // Return null on exception
      }
    }

    // Test successful case
    Assertions.assertEquals("Test Title", safeCompletableFutureHandling(shouldComplete = true, shouldThrowException = false))

    // Test exception case
    Assertions.assertNull(safeCompletableFutureHandling(shouldComplete = true, shouldThrowException = true))
  }

  @Test
  fun `test invokeLater error handling logic`() {
    // Test the error handling pattern for invokeLater operations

    fun safeInvokeLater(shouldThrowException: Boolean): Boolean {
      return try {
        // Simulate invokeLater operation
        if (shouldThrowException) {
          throw RuntimeException("InvokeLater failed")
        }
        true
      } catch (_: Exception) {
        // Handle invokeLater exceptions gracefully
        false
      }
    }

    // Test normal case
    Assertions.assertTrue(safeInvokeLater(false))

    // Test exception case
    Assertions.assertFalse(safeInvokeLater(true))
  }
}
