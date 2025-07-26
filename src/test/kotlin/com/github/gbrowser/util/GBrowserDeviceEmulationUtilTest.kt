package com.github.gbrowser.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GBrowserDeviceEmulationUtilTest {

  @Test
  fun `test device profiles are properly defined`() {
    val profiles = GBrowserDeviceEmulationUtil.DEVICE_PROFILES

    // Verify we have device profiles
    assertTrue(profiles.isNotEmpty())

    // Verify some expected devices exist
    assertTrue(profiles.containsKey("iPhone SE"))
    assertTrue(profiles.containsKey("iPad Pro"))
    assertTrue(profiles.containsKey("Pixel 7"))

    // Verify all profiles have required properties
    profiles.values.forEach { profile ->
      assertTrue(profile.name.isNotEmpty())
      assertTrue(profile.width > 0)
      assertTrue(profile.height > 0)
      assertTrue(profile.deviceScaleFactor > 0)
      assertTrue(profile.userAgent.isNotEmpty())
    }
  }

  @Test
  fun `test device profiles have valid dimensions`() {
    GBrowserDeviceEmulationUtil.DEVICE_PROFILES.values.forEach { profile ->
      // Verify reasonable dimensions
      assertTrue(profile.width in 320..2560, "Width ${profile.width} out of reasonable range for ${profile.name}")
      assertTrue(profile.height in 480..2732, "Height ${profile.height} out of reasonable range for ${profile.name}")
      assertTrue(profile.deviceScaleFactor in 1.0..4.0, "DPR ${profile.deviceScaleFactor} out of reasonable range for ${profile.name}")
    }
  }

  @Test
  fun `test mobile devices have touch enabled`() {
    val mobileDevices = listOf("iPhone", "iPad", "Pixel", "Galaxy")

    GBrowserDeviceEmulationUtil.DEVICE_PROFILES.entries
      .filter { entry -> mobileDevices.any { entry.key.contains(it) } }
      .forEach { entry ->
        assertTrue(entry.value.isMobile, "${entry.key} should be marked as mobile")
        assertTrue(entry.value.hasTouch, "${entry.key} should have touch enabled")
      }
  }

  @Test
  fun `test escape function handles all special characters`() {
    // Use reflection to test private escape function
    val escapeMethod = GBrowserDeviceEmulationUtil::class.java
      .getDeclaredMethod("escapeJavaScriptString", String::class.java)
    escapeMethod.isAccessible = true

    val testCases = mapOf(
      "normal text" to "normal text",
      "text with 'quotes'" to "text with \\'quotes\\'",
      "text with \"double quotes\"" to "text with \\\"double quotes\\\"",
      "text with \n newline" to "text with \\n newline",
      "text with \r carriage return" to "text with \\r carriage return",
      "text with \t tab" to "text with \\t tab",
      "text with \\ backslash" to "text with \\\\ backslash",
      "text with \u2028 line sep" to "text with \\u2028 line sep",
      "text with \u2029 para sep" to "text with \\u2029 para sep"
    )

    testCases.forEach { (input, expected) ->
      val result = escapeMethod.invoke(GBrowserDeviceEmulationUtil, input) as String
      assertEquals(expected, result, "Failed to escape: $input")
    }
  }

  @Test
  fun `test JavaScript injection prevention with escape function`() {
    // Use reflection to test private escape function
    val escapeMethod = GBrowserDeviceEmulationUtil::class.java
      .getDeclaredMethod("escapeJavaScriptString", String::class.java)
    escapeMethod.isAccessible = true

    // Test various malicious inputs
    val maliciousInputs = listOf(
      "'; alert('XSS'); '",
      "\"; alert('XSS'); \"",
      "</script><script>alert('XSS')</script>",
      "\\'; alert('XSS'); \\'",
      "\u2028alert('XSS')\u2029"
    )

    maliciousInputs.forEach { maliciousInput ->
      val escaped = escapeMethod.invoke(GBrowserDeviceEmulationUtil, maliciousInput) as String

      // Verify quotes are properly escaped
      if (maliciousInput.contains("'") && !maliciousInput.startsWith("\\'")) {
        // Count unescaped single quotes vs escaped ones
        val unescapedQuotes = maliciousInput.count { it == '\'' }
        val escapedQuotes = escaped.split("\\'").size - 1
        assertEquals(unescapedQuotes, escapedQuotes, "All single quotes should be escaped")
      }

      if (maliciousInput.contains("\"") && !maliciousInput.startsWith("\\\"")) {
        // Count unescaped double quotes vs escaped ones
        val unescapedQuotes = maliciousInput.count { it == '"' }
        val escapedQuotes = escaped.split("\\\"").size - 1
        assertEquals(unescapedQuotes, escapedQuotes, "All double quotes should be escaped")
      }

      // Verify special characters are escaped
      if (maliciousInput.contains("\u2028")) {
        assertTrue(escaped.contains("\\u2028"))
      }
      if (maliciousInput.contains("\u2029")) {
        assertTrue(escaped.contains("\\u2029"))
      }

      // The escaped string should not equal the original (unless it had no escapable chars)
      if (maliciousInput.contains("'") || maliciousInput.contains("\"") ||
        maliciousInput.contains("\\") || maliciousInput.contains("\n") ||
        maliciousInput.contains("\r") || maliciousInput.contains("\t")) {
        assertNotEquals(maliciousInput, escaped)
      }
    }
  }

  @Test
  fun `test device profile user agents are properly formatted`() {
    GBrowserDeviceEmulationUtil.DEVICE_PROFILES.values.forEach { profile ->
      // Verify user agent contains basic required components
      assertTrue(profile.userAgent.contains("Mozilla"), "User agent should contain Mozilla for ${profile.name}")
      assertTrue(profile.userAgent.isNotBlank(), "User agent should not be blank for ${profile.name}")

      // Mobile devices should have mobile indicators
      // Exceptions: Surface Pro and Asus Zenbook Fold are foldable/convertible devices
      // Nest Hub devices are smart displays with custom user agents
      val specialDevices = listOf("Surface", "Zenbook Fold", "Nest Hub")
      if (profile.isMobile && specialDevices.none { profile.name.contains(it) }) {
        assertTrue(
          profile.userAgent.contains("Mobile") || profile.userAgent.contains("Android") || profile.userAgent.contains("iPhone"),
          "Mobile device ${profile.name} should have mobile indicator in user agent"
        )
      }
    }
  }
}