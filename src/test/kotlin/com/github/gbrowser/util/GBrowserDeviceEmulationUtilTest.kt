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

  @Test
  fun `test special device profiles have correct properties`() {
    // Test Surface devices
    val surfacePro = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Surface Pro 7"]
    assertNotNull(surfacePro)
    assertEquals("Surface Pro 7", surfacePro!!.name)
    assertTrue(surfacePro.isMobile, "Surface Pro should be marked as mobile for touch support")
    assertTrue(surfacePro.hasTouch, "Surface Pro should have touch enabled")
    assertTrue(surfacePro.userAgent.contains("Windows NT"), "Surface Pro should have Windows user agent")
    assertFalse(surfacePro.userAgent.contains("Mobile"), "Surface Pro should not have Mobile in user agent")

    val surfaceDuo = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Surface Duo"]
    assertNotNull(surfaceDuo)
    assertTrue(surfaceDuo!!.userAgent.contains("Android"), "Surface Duo should have Android user agent")
    assertTrue(surfaceDuo.userAgent.contains("Mobile"), "Surface Duo should have Mobile in user agent")
  }

  @Test
  fun `test Nest Hub devices have special user agents`() {
    val nestHub = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Nest Hub"]
    assertNotNull(nestHub)
    assertEquals("Nest Hub", nestHub!!.name)
    assertTrue(nestHub.isMobile, "Nest Hub should be marked as mobile")
    assertTrue(nestHub.hasTouch, "Nest Hub should have touch enabled")
    assertTrue(nestHub.userAgent.contains("CrKey"), "Nest Hub should have CrKey identifier")
    assertTrue(nestHub.userAgent.contains("X11; Linux armv7l"), "Nest Hub should have Linux ARM user agent")

    val nestHubMax = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Nest Hub Max"]
    assertNotNull(nestHubMax)
    assertTrue(nestHubMax!!.userAgent.contains("CrKey"), "Nest Hub Max should have CrKey identifier")
    assertTrue(nestHubMax.userAgent.contains("X11; Linux aarch64"), "Nest Hub Max should have Linux ARM64 user agent")
  }

  @Test
  fun `test foldable devices have appropriate settings`() {
    val zenbookFold = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Asus Zenbook Fold"]
    assertNotNull(zenbookFold)
    assertEquals("Asus Zenbook Fold", zenbookFold!!.name)
    assertTrue(zenbookFold.isMobile, "Zenbook Fold should be marked as mobile for touch support")
    assertTrue(zenbookFold.hasTouch, "Zenbook Fold should have touch enabled")
    assertTrue(zenbookFold.userAgent.contains("Windows NT"), "Zenbook Fold should have Windows user agent")

    val galaxyFold = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Galaxy Z Fold 5"]
    assertNotNull(galaxyFold)
    assertTrue(galaxyFold!!.userAgent.contains("Android"), "Galaxy Fold should have Android user agent")
    assertTrue(galaxyFold.userAgent.contains("Mobile"), "Galaxy Fold should have Mobile in user agent")
  }

  @Test
  fun `test escapeJavaScriptString handles empty and null-like inputs`() {
    val escapeMethod = GBrowserDeviceEmulationUtil::class.java
      .getDeclaredMethod("escapeJavaScriptString", String::class.java)
    escapeMethod.isAccessible = true

    // Test empty string
    assertEquals("", escapeMethod.invoke(GBrowserDeviceEmulationUtil, ""))

    // Test strings that could be confused with null
    assertEquals("null", escapeMethod.invoke(GBrowserDeviceEmulationUtil, "null"))
    assertEquals("undefined", escapeMethod.invoke(GBrowserDeviceEmulationUtil, "undefined"))
  }

  @Test
  fun `test escapeJavaScriptString handles complex nested quotes`() {
    val escapeMethod = GBrowserDeviceEmulationUtil::class.java
      .getDeclaredMethod("escapeJavaScriptString", String::class.java)
    escapeMethod.isAccessible = true

    // Test complex nested quotes
    val complexInput = """He said, "She's saying 'Hello' to everyone."""
    val escaped = escapeMethod.invoke(GBrowserDeviceEmulationUtil, complexInput) as String
    
    // Should escape both single and double quotes
    assertTrue(escaped.contains("\\\""))
    assertTrue(escaped.contains("\\'"))
    
    // The escaped string should have properly escaped all quotes
    // Verify that the specific escape patterns are present
    assertTrue(escaped.contains("\\\"She\\'s"), "Should contain escaped double quote before She's")
    assertTrue(escaped.contains("\\'Hello\\'"), "Should contain escaped single quotes around Hello")
    
    // The escaped string should look like this
    assertEquals("""He said, \"She\'s saying \'Hello\' to everyone.""", escaped)
  }

  @Test
  fun `test device profiles cover various screen sizes`() {
    val profiles = GBrowserDeviceEmulationUtil.DEVICE_PROFILES.values
    
    // Test we have small phones
    assertTrue(profiles.any { it.width < 400 }, "Should have small phone devices")
    
    // Test we have tablets
    assertTrue(profiles.any { it.width >= 768 && it.width < 1024 }, "Should have tablet devices")
    
    // Test we have large tablets/hybrids
    assertTrue(profiles.any { it.width >= 1024 }, "Should have large tablet/hybrid devices")
    
    // Test various pixel densities
    assertTrue(profiles.any { it.deviceScaleFactor == 2.0 }, "Should have 2x DPR devices")
    assertTrue(profiles.any { it.deviceScaleFactor == 3.0 }, "Should have 3x DPR devices")
    assertTrue(profiles.any { it.deviceScaleFactor > 3.0 }, "Should have high DPR devices")
  }

  @Test
  fun `test JavaScript generation for device emulation`() {
    val profile = DeviceProfile(
      name = "Test Device",
      width = 320,
      height = 568,
      deviceScaleFactor = 2.0,
      userAgent = "Test User Agent",
      isMobile = true,
      hasTouch = true
    )

    // We can't directly test the JavaScript execution, but we can verify the profile properties
    assertEquals(320, profile.width)
    assertEquals(568, profile.height)
    assertEquals(2.0, profile.deviceScaleFactor)
    assertEquals("Test User Agent", profile.userAgent)
    assertTrue(profile.isMobile)
    assertTrue(profile.hasTouch)
  }

  @Test
  fun `test edge case device dimensions`() {
    // Test minimum reasonable dimensions
    val smallProfile = DeviceProfile(
      name = "Small Device",
      width = 320,
      height = 480,
      deviceScaleFactor = 1.0,
      userAgent = "Small Device UA"
    )

    assertTrue(smallProfile.width >= 320, "Width should be at least 320px")
    assertTrue(smallProfile.height >= 480, "Height should be at least 480px")

    // Test maximum reasonable dimensions
    val largeProfile = DeviceProfile(
      name = "Large Device",
      width = 1920,
      height = 1080,
      deviceScaleFactor = 1.0,
      userAgent = "Large Device UA"
    )

    assertTrue(largeProfile.width <= 1920, "Width should be at most 1920px for mobile/tablet")
    assertTrue(largeProfile.height <= 1080, "Height should be at most 1080px for landscape")
  }

  @Test
  fun `test device profile consistency`() {
    GBrowserDeviceEmulationUtil.DEVICE_PROFILES.forEach { (name, profile) ->
      // Verify profile name matches map key
      assertEquals(name, profile.name, "Profile name should match map key")

      // Verify dimensions make sense (width or height should be larger)
      val maxDimension = maxOf(profile.width, profile.height)
      val minDimension = minOf(profile.width, profile.height)
      assertTrue(
        maxDimension > minDimension || maxDimension == minDimension,
        "Dimensions should be valid for ${profile.name}"
      )

      // Verify aspect ratio is reasonable (between 1:3 and 3:1)
      val aspectRatio = maxDimension.toDouble() / minDimension
      assertTrue(aspectRatio <= 3.0, "Aspect ratio too extreme for ${profile.name}: $aspectRatio")
    }
  }

  @Test
  fun `test custom device profile creation`() {
    val customProfile = DeviceProfile(
      name = "Custom Device",
      width = 1440,
      height = 900,
      deviceScaleFactor = 1.5,
      userAgent = "Custom Device User Agent",
      isMobile = false,
      hasTouch = false
    )

    assertEquals("Custom Device", customProfile.name)
    assertEquals(1440, customProfile.width)
    assertEquals(900, customProfile.height)
    assertEquals(1.5, customProfile.deviceScaleFactor)
    assertFalse(customProfile.isMobile)
    assertFalse(customProfile.hasTouch)
  }

  @Test
  fun `test device categories coverage`() {
    val profiles = GBrowserDeviceEmulationUtil.DEVICE_PROFILES

    // Check we have various device categories
    assertTrue(profiles.keys.any { it.contains("iPhone") }, "Should have iPhone devices")
    assertTrue(profiles.keys.any { it.contains("iPad") }, "Should have iPad devices")
    assertTrue(profiles.keys.any { it.contains("Pixel") || it.contains("Galaxy") }, "Should have Android devices")
    assertTrue(profiles.keys.any { it.contains("Surface") }, "Should have Surface devices")
    assertTrue(profiles.keys.any { it.contains("Fold") }, "Should have foldable devices")
    assertTrue(profiles.keys.any { it.contains("Nest") }, "Should have smart display devices")
  }

  @Test
  fun `test user agent format validation`() {
    val userAgentRegex = Regex("Mozilla/[0-9.]+ \\([^)]+\\) .+")

    GBrowserDeviceEmulationUtil.DEVICE_PROFILES.values.forEach { profile ->
      assertTrue(
        profile.userAgent.matches(userAgentRegex),
        "User agent should follow standard format for ${profile.name}: ${profile.userAgent}"
      )
    }
  }

  @Test
  fun `test device scale factor ranges`() {
    val validScaleFactors = setOf(1.0, 1.5, 2.0, 2.5, 2.625, 3.0, 3.5, 4.0)

    GBrowserDeviceEmulationUtil.DEVICE_PROFILES.values.forEach { profile ->
      assertTrue(
        profile.deviceScaleFactor in validScaleFactors,
        "Device scale factor ${profile.deviceScaleFactor} should be a standard value for ${profile.name}"
      )
    }
  }

  @Test
  fun `test escape function handles Unicode correctly`() {
    val escapeMethod = GBrowserDeviceEmulationUtil::class.java
      .getDeclaredMethod("escapeJavaScriptString", String::class.java)
    escapeMethod.isAccessible = true

    // Test Unicode handling
    val unicodeTests = mapOf(
      "Hello 世界" to "Hello 世界", // Chinese characters should pass through
      "Emoji 😀" to "Emoji 😀", // Emojis should pass through
      "Mixed 'quotes' and 世界" to "Mixed \\'quotes\\' and 世界",
      "Line\u2028Separator" to "Line\\u2028Separator",
      "Para\u2029Separator" to "Para\\u2029Separator"
    )

    unicodeTests.forEach { (input, expected) ->
      val result = escapeMethod.invoke(GBrowserDeviceEmulationUtil, input) as String
      assertEquals(expected, result, "Failed to handle Unicode in: $input")
    }
  }

  @Test
  fun `test responsive mode profile`() {
    // Test creating a responsive mode profile
    val responsiveProfile = DeviceProfile(
      name = "Responsive",
      width = 400,
      height = 626,
      deviceScaleFactor = 1.0,
      userAgent = "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36",
      isMobile = true,
      hasTouch = true
    )

    assertEquals("Responsive", responsiveProfile.name)
    assertEquals(400, responsiveProfile.width)
    assertEquals(626, responsiveProfile.height)
    assertEquals(1.0, responsiveProfile.deviceScaleFactor)
    assertTrue(responsiveProfile.isMobile)
    assertTrue(responsiveProfile.hasTouch)
  }

  @Test
  fun `test profile data class equality`() {
    val profile1 = DeviceProfile(
      name = "Test",
      width = 400,
      height = 800,
      deviceScaleFactor = 2.0,
      userAgent = "Test UA"
    )

    val profile2 = DeviceProfile(
      name = "Test",
      width = 400,
      height = 800,
      deviceScaleFactor = 2.0,
      userAgent = "Test UA"
    )

    val profile3 = DeviceProfile(
      name = "Different",
      width = 400,
      height = 800,
      deviceScaleFactor = 2.0,
      userAgent = "Test UA"
    )

    assertEquals(profile1, profile2, "Identical profiles should be equal")
    assertNotEquals(profile1, profile3, "Different profiles should not be equal")
  }

  @Test
  fun `test escapeJavaScriptString performance with large input`() {
    val escapeMethod = GBrowserDeviceEmulationUtil::class.java
      .getDeclaredMethod("escapeJavaScriptString", String::class.java)
    escapeMethod.isAccessible = true

    // Create a large string with various escapable characters
    val largeInput = buildString {
      repeat(1000) {
        append("Line $it with 'quotes' and \"double quotes\" and \n newlines\t")
      }
    }

    val startTime = System.currentTimeMillis()
    val result = escapeMethod.invoke(GBrowserDeviceEmulationUtil, largeInput) as String
    val endTime = System.currentTimeMillis()

    // Should complete reasonably quickly (under 100ms for 1000 iterations)
    assertTrue(endTime - startTime < 100, "Escape function took too long: ${endTime - startTime}ms")

    // Verify escaping worked
    assertTrue(result.contains("\\'"))
    assertTrue(result.contains("\\\""))
    assertTrue(result.contains("\\n"))
    assertTrue(result.contains("\\t"))
  }
}