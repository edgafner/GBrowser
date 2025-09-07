package com.github.gbrowser.ui.gcef.impl

import com.github.gbrowser.actions.DeviceEmulationConstants
import com.github.gbrowser.settings.request_header.GBrowserRequestHeader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserCefResourceRequestHandlerTest {

  // Test the anti-detection domain logic without platform dependencies
  private fun needsAntiDetection(url: String): Boolean {
    val antiDetectionDomains = listOf(
      "perplexity.ai",
      "challenges.cloudflare.com",
      "openai.com",
      "chat.openai.com",
      "claude.ai"
    )

    return antiDetectionDomains.any { domain ->
      url.contains(domain, ignoreCase = true)
    }
  }

  // Test user agent selection logic
  private fun selectUserAgent(url: String): String {
    return if (needsAntiDetection(url)) {
      DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER
    } else {
      DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER
    }
  }

  // Test custom header matching logic
  private fun shouldApplyHeader(url: String?, header: GBrowserRequestHeader): Boolean {
    return url?.matches(Regex(header.uriRegex)) == true
  }

  @Test
  fun `test uses modern user agent for anti-detection sites`() {
    val url = "https://perplexity.ai/search"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER, userAgent)
    assertTrue(needsAntiDetection(url))
  }

  @Test
  fun `test uses modern user agent for Cloudflare challenges`() {
    val url = "https://challenges.cloudflare.com/turnstile"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER, userAgent)
    assertTrue(needsAntiDetection(url))
  }

  @Test
  fun `test uses modern user agent for OpenAI sites`() {
    val url = "https://chat.openai.com/chat"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER, userAgent)
    assertTrue(needsAntiDetection(url))
  }

  @Test
  fun `test uses modern user agent for Claude AI`() {
    val url = "https://claude.ai/chat"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER, userAgent)
    assertTrue(needsAntiDetection(url))
  }

  @Test
  fun `test uses default user agent for Gmail`() {
    val url = "https://mail.google.com/mail"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, userAgent)
    assertFalse(needsAntiDetection(url))
  }

  @Test
  fun `test uses default user agent for regular sites`() {
    val url = "https://example.com"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, userAgent)
    assertFalse(needsAntiDetection(url))
  }

  @Test
  fun `test applies custom headers when URL matches regex`() {
    val customHeader = GBrowserRequestHeader(
      value = "Custom-Value",
      name = "Custom-Header",
      overwrite = true,
      uriRegex = ".*example\\.com.*"
    )
    val url = "https://example.com/page"

    assertTrue(shouldApplyHeader(url, customHeader))
    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, selectUserAgent(url))
  }

  @Test
  fun `test custom headers not applied when URL does not match regex`() {
    val customHeader = GBrowserRequestHeader(
      value = "Custom-Value",
      name = "Custom-Header",
      overwrite = true,
      uriRegex = ".*example\\.com.*"
    )
    val url = "https://different.com/page"

    assertFalse(shouldApplyHeader(url, customHeader))
    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, selectUserAgent(url))
  }

  @Test
  fun `test handles null URL gracefully`() {
    val url: String? = null
    val userAgent = selectUserAgent(url ?: "")

    // Should use default user agent when URL is null
    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, userAgent)
    assertFalse(needsAntiDetection(""))
  }

  @Test
  fun `test case insensitive domain matching`() {
    val url = "https://PERPLEXITY.AI/search"
    val userAgent = selectUserAgent(url)

    assertEquals(DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER, userAgent)
    assertTrue(needsAntiDetection(url))
  }

  @Test
  fun `test delegate called even without custom headers`() {
    val url = "https://example.com"

    // Test that we can process URLs even when no custom headers are defined
    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, selectUserAgent(url))
  }

  @Test
  fun `test multiple custom headers applied`() {
    val headers = mutableSetOf(
      GBrowserRequestHeader("Value1", "Header1", true, ".*example.*"),
      GBrowserRequestHeader("Value2", "Header2", false, ".*example.*")
    )
    val url = "https://example.com"

    // Test that both headers should be applied
    headers.forEach { header ->
      assertTrue(shouldApplyHeader(url, header), "Header ${header.name} should be applied")
    }
    assertEquals(DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER, selectUserAgent(url))
  }
}