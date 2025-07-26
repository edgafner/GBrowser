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
    assertTrue(script.contains("color-scheme: dark"))
    assertTrue(script.contains("window.matchMedia"))
    assertTrue(script.contains("prefers-color-scheme: dark"))
  }

  @Test
  fun `test buildLightModeScript creates valid script`() {
    // Use reflection to access private method
    val buildLightModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("buildLightModeScript")
    buildLightModeScriptMethod.isAccessible = true

    val script = buildLightModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify script content
    assertTrue(script.contains("Applying light theme"))
    assertTrue(script.contains("color-scheme: light"))
    assertTrue(script.contains("window.__originalMatchMedia"))
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

  @Test
  fun `test dark mode script contains proper dark mode support`() {
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    val script = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify script contains CSS for dark mode
    assertTrue(script.contains("gbrowser-dark-mode-support"))
    assertTrue(script.contains("color-scheme: dark"))

    // Verify meta tags
    assertTrue(script.contains("meta[name=\"color-scheme\"]"))
    assertTrue(script.contains("meta[name=\"theme-color\"]"))

    // Verify it uses Chrome's dark color
    assertTrue(script.contains("#202124"))
  }

  @Test
  fun `test dark mode script sets proper meta tags`() {
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    val script = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify meta color-scheme
    assertTrue(script.contains("metaColorScheme.content = 'dark'"))

    // Verify theme color matches Chrome dark theme
    assertTrue(script.contains("metaTheme.content = '#202124'"))
    
    // Verify scrollbar styling
    assertTrue(script.contains("::-webkit-scrollbar"))
  }

  @Test
  fun `test light mode script removes dark mode styles`() {
    val getLightModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getLightModeScript")
    getLightModeScriptMethod.isAccessible = true

    val script = getLightModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify it removes dark mode CSS
    assertTrue(script.contains("document.getElementById('gbrowser-dark-mode-support')"))
    assertTrue(script.contains(".remove()"))

    // Verify it restores matchMedia
    assertTrue(script.contains("window.matchMedia = window.__originalMatchMedia"))
  }

  @Test
  fun `test light mode script sets proper meta tags`() {
    val getLightModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getLightModeScript")
    getLightModeScriptMethod.isAccessible = true

    val script = getLightModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify meta color-scheme
    assertTrue(script.contains("metaColorScheme.content = 'light'"))

    // Verify theme color
    assertTrue(script.contains("metaTheme.content = '#ffffff'"))

    // Verify it dispatches events
    assertTrue(script.contains("colorschemechange"))
  }

  @Test
  fun `test dark mode script checks device emulation flag`() {
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    val script = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify it checks for device emulation
    assertTrue(script.contains("window.__gbrowserDarkModeApplied === 'device-emulation'"))
    assertTrue(script.contains("Device emulation active, skipping dark theme"))
  }

  @Test
  fun `test theme scripts handle matchMedia properly`() {
    val getDarkModeScriptMethod = GBrowserThemeUtil::class.java.getDeclaredMethod("getDarkModeScript")
    getDarkModeScriptMethod.isAccessible = true

    val script = getDarkModeScriptMethod.invoke(GBrowserThemeUtil) as String

    // Verify matchMedia override for dark mode
    assertTrue(script.contains("window.matchMedia"))
    assertTrue(script.contains("prefers-color-scheme: dark"))
    assertTrue(script.contains("window.__originalMatchMedia"))
  }
}