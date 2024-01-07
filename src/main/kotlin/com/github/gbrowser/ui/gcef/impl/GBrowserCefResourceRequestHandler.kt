package com.github.gbrowser.ui.gcef.impl

import com.github.gbrowser.ui.gcef.GBrowserCefRequestDelegate
import com.github.gbrowser.settings.GBrowserService
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.cef.network.CefURLRequest.Status

class GBrowserCefResourceRequestHandler(private val delegate: GBrowserCefRequestDelegate? = null) : CefResourceRequestHandlerAdapter() {

  override fun onBeforeResourceLoad(browser: CefBrowser?, frame: CefFrame?, request: CefRequest): Boolean {
    request.setHeaderByName("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 /CefSharp Browser 90.0",
                            true)
    GBrowserService.instance().requestHeaders.forEach { header ->
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
