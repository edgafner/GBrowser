package com.github.gbrowser.ui.gcef.impl


import com.github.gbrowser.ui.gcef.GBrowserCefRequestDelegate
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.network.CefRequest

class GBrowserCefRequestHandler(private val delegate: GBrowserCefRequestDelegate? = null) : CefRequestHandlerAdapter() {

  override fun getResourceRequestHandler(browser: CefBrowser?,
                                         frame: CefFrame?,
                                         request: CefRequest?,
                                         isNavigation: Boolean,
                                         isDownload: Boolean,
                                         requestInitiator: String?,
                                         disableDefaultHandling: BoolRef?): CefResourceRequestHandler {
    return GBrowserCefResourceRequestHandler(delegate)
  }
}
