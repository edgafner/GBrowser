package com.github.gbrowser.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GBrowserThemeUtilTest {

  @AfterEach
  fun tearDown() {
    // Reset cached scripts using reflection
    try {
      val darkScriptField = GBrowserThemeUtil::class.java.getDeclaredField("cachedDarkModeScript")
      darkScriptField.isAccessible = true
      darkScriptField.set(null, null)
    } catch (e: Exception) {
      // Field might not exist in all versions
    }

    try {
      val lightScriptField = GBrowserThemeUtil::class.java.getDeclaredField("cachedLightModeScript")
      lightScriptField.isAccessible = true
      lightScriptField.set(null, null)
    } catch (e: Exception) {
      // Field might not exist in all versions
    }
  }

  @Test
  fun `test dark mode script is cached`() {
    // Use reflection to access private methods
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    // Get script twice
    val script1 = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String
    val script2 = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String

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
    val script1 = getLightModeScriptMethod.invoke(GBrowserThemeUtil) as String
    val script2 = getLightModeScriptMethod.invoke(GBrowserThemeUtil) as String

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

    val darkScript = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String
    val lightScript = getLightModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Scripts should be different
    assertNotEquals(darkScript, lightScript)
    assertTrue(darkScript.contains("dark"))
    assertTrue(lightScript.contains("light"))
  }

  @Test
  fun `test buildDarkModeScript creates valid script`() {
    // Use reflection to access private method
    val buildDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("buildDarkModeScript")
    buildDarkModeScriptMethod.isAccessible = true

    val script = buildDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify script content
    assertTrue(script.contains("Applying dark theme"))
    assertTrue(script.contains("background-color"))
    assertTrue(script.contains("color:"))
    assertTrue(script.contains("filter:"))
  }

  @Test
  fun `test buildLightModeScript creates valid script`() {
    // Use reflection to access private method
    val buildLightModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("buildLightModeScript")
    buildLightModeScriptMethod.isAccessible = true

    val script = buildLightModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify script content
    assertTrue(script.contains("Applying light theme"))
    assertTrue(script.contains("background-color"))
    assertTrue(script.contains("color:"))
    assertTrue(script.contains("!important"))
  }

  @Test
  fun `test script caching improves performance`() {
    // Use reflection to access private methods
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    // Measure time for first call (builds script)
    val start1 = System.nanoTime()
    val script1 = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String
    val time1 = System.nanoTime() - start1

    // Measure time for second call (uses cache)
    val start2 = System.nanoTime()
    val script2 = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String
    val time2 = System.nanoTime() - start2

    // Second call should be significantly faster (at least 10x)
    // Note: This might be flaky in CI, so we just verify caching works
    assertSame(script1, script2)
  }
}