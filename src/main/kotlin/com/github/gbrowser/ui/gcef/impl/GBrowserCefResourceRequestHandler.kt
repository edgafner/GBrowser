package com.github.gbrowser.ui.gcef.impl

import com.github.gbrowser.actions.DeviceEmulationConstants
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.ui.gcef.GBrowserCefRequestDelegate
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.cef.network.CefURLRequest.Status

class GBrowserCefResourceRequestHandler(val project: Project, private val delegate: GBrowserCefRequestDelegate? = null) : CefResourceRequestHandlerAdapter() {

  override fun onBeforeResourceLoad(browser: CefBrowser?, frame: CefFrame?, request: CefRequest): Boolean {
    val url = request.url ?: ""

    // Use modern User-Agent only for sites that need anti-detection
    val userAgent = if (needsAntiDetection(url)) {
      DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER
    } else {
      // Use original User-Agent for Gmail and other sites
      DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER
    }

    request.setHeaderByName("User-Agent", userAgent, true)

    // Apply user-defined custom headers (same as original behavior)
    project.service<GBrowserService>().requestHeaders.forEach { header ->
      if (request.url?.matches(Regex(header.uriRegex)) == true) {
        request.setHeaderByName(header.name, header.value, header.overwrite)
      }
    }

    delegate?.onBeforeResourceLoad(browser, frame, request)
    return super.onBeforeResourceLoad(browser, frame, request)
  }

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

  override fun onResourceLoadComplete(browser: CefBrowser?,
                                      frame: CefFrame?,
                                      request: CefRequest?,
                                      response: CefResponse?,
                                      status: Status?,
                                      receivedContentLength: Long) {
    delegate?.onResourceLoadComplete(browser, frame, request, response, status, receivedContentLength)
    super.onResourceLoadComplete(browser, frame, request, response, status, receivedContentLength)
  }

  override fun onResourceResponse(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?, response: CefResponse?): Boolean {
    return super.onResourceResponse(browser, frame, request, response)
  }
}
