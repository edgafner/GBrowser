package com.github.gbrowser.util

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.theme.GBrowserTheme
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GBrowserThemeUtilTest {

  private lateinit var mockService: GBrowserService
  private lateinit var mockProject: Project

  @BeforeEach
  fun setUp() {
    // Initialize mocks
    mockService = mockk(relaxed = true)
    mockProject = mockk(relaxed = true)

    // Mock the service call
    mockkStatic("com.intellij.openapi.components.ServiceManagerKt")
    every { mockProject.service<GBrowserService>() } returns mockService
  }

  @AfterEach
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
    // Reset cached scripts using reflection
    val darkScriptField = GBrowserThemeUtil::class.java.getDeclaredField("cachedDarkModeScript")
    darkScriptField.isAccessible = true
    darkScriptField.set(null, null)

    val lightScriptField = GBrowserThemeUtil::class.java.getDeclaredField("cachedLightModeScript")
    lightScriptField.isAccessible = true
    lightScriptField.set(null, null)
  }

  @Test
  fun `test isDarkTheme returns correct value for DARK theme`() {
    every { mockService.theme } returns GBrowserTheme.DARK

    assertTrue(GBrowserThemeUtil.isDarkTheme(mockProject))
  }

  @Test
  fun `test isDarkTheme returns correct value for LIGHT theme`() {
    every { mockService.theme } returns GBrowserTheme.LIGHT

    assertFalse(GBrowserThemeUtil.isDarkTheme(mockProject))
  }

  @Test
  fun `test isDarkTheme follows IDE theme when set to FOLLOW_IDE`() {
    every { mockService.theme } returns GBrowserTheme.FOLLOW_IDE

    // Mock JBColor.isBright()
    mockkStatic(JBColor::class)

    // Test dark IDE theme
    every { JBColor.isBright() } returns false
    assertTrue(GBrowserThemeUtil.isDarkTheme(mockProject))

    // Test light IDE theme
    every { JBColor.isBright() } returns true
    assertFalse(GBrowserThemeUtil.isDarkTheme(mockProject))
  }

  @Test
  fun `test dark mode script is cached`() {
    // Use reflection to access private methods
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    // Get script twice
    val script1 = getDarkModeScriptMethod.invoke(null) as String
    val script2 = getDarkModeScriptMethod.invoke(null) as String

    // Should be the same instance (cached)
    assertSame(script1, script2)

    // Script should contain dark theme content
    assertTrue(script1.contains("Applying dark theme"))
  }

  @Test
  fun `test light mode script is cached`() {
    // Use reflection to access private methods
    val getLightModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getLightModeScript")
    getLightModeScriptMethod.isAccessible = true

    // Get script twice
    val script1 = getLightModeScriptMethod.invoke(null) as String
    val script2 = getLightModeScriptMethod.invoke(null) as String

    // Should be the same instance (cached)
    assertSame(script1, script2)

    // Script should contain light theme content
    assertTrue(script1.contains("Applying light theme"))
  }

  @Test
  fun `test dark and light scripts are different`() {
    // Use reflection to access private methods
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true
    val getLightModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getLightModeScript")
    getLightModeScriptMethod.isAccessible = true

    val darkScript = getDarkModeScriptMethod.invoke(null) as String
    val lightScript = getLightModeScriptMethod.invoke(null) as String

    // Scripts should be different
    assertNotEquals(darkScript, lightScript)
    assertTrue(darkScript.contains("dark"))
    assertTrue(lightScript.contains("light"))
  }
}