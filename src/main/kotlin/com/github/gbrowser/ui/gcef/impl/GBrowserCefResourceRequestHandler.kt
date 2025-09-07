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
    // Set modern, realistic User-Agent without automation indicators
    request.setHeaderByName(
      "User-Agent",
      DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER,
      true
    )

    // Add common browser headers to appear more legitimate
    val isMainFrameRequest = frame?.isMain == true

    request.setHeaderByName("Accept-Language", "en-US,en;q=0.9", false)
    request.setHeaderByName("Accept-Encoding", "gzip, deflate, br, zstd", false)
    request.setHeaderByName("Sec-Ch-Ua", "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"", false)
    request.setHeaderByName("Sec-Ch-Ua-Mobile", "?0", false)
    request.setHeaderByName("Sec-Ch-Ua-Platform", "\"Windows\"", false)

    // Add different headers based on request type
    if (isMainFrameRequest) {
      request.setHeaderByName(
        "Accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        false
      )
      request.setHeaderByName("Sec-Fetch-Dest", "document", false)
      request.setHeaderByName("Sec-Fetch-Mode", "navigate", false)
      request.setHeaderByName("Sec-Fetch-Site", "none", false)
      request.setHeaderByName("Sec-Fetch-User", "?1", false)
      request.setHeaderByName("Upgrade-Insecure-Requests", "1", false)
    } else {
      // For sub-resources
      request.setHeaderByName("Sec-Fetch-Dest", "empty", false)
      request.setHeaderByName("Sec-Fetch-Mode", "cors", false)
      request.setHeaderByName("Sec-Fetch-Site", "cross-site", false)
    }

    // Remove potential automation detection headers if they exist
    request.setHeaderByName("Webdriver", "", true) // Remove webdriver header
    request.setHeaderByName("X-Forwarded-For", "", true) // Clear forwarding headers

    // Apply user-defined custom headers
    project.service<GBrowserService>().requestHeaders.forEach { header ->
      if (request.url?.matches(Regex(header.uriRegex)) == true) {
        request.setHeaderByName(header.name, header.value, header.overwrite)
      }
    }

    delegate?.onBeforeResourceLoad(browser, frame, request)
    return super.onBeforeResourceLoad(browser, frame, request)
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
