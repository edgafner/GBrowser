package com.github.gbrowser.ui.gcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.cef.network.CefURLRequest

interface GBrowserCefRequestDelegate {

  fun onBeforeResourceLoad(cefBrowser: CefBrowser?, cefFrame: CefFrame?, cefRequest: CefRequest)

  fun onResourceLoadComplete(cefBrowser: CefBrowser?,
                             cefFrame: CefFrame?,
                             cefRequest: CefRequest?,
                             cefResponse: CefResponse?,
                             status: CefURLRequest.Status?,
                             receivedContentLength: Long)
}