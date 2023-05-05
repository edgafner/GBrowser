package com.github.gib.gcef

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.network.CefRequest

class GBRequestHandler : CefRequestHandlerAdapter() {
    override fun getResourceRequestHandler(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?, isNavigation: Boolean, isDownload: Boolean, requestInitiator: String?, disableDefaultHandling: BoolRef?): CefResourceRequestHandler {
        return GBCefResourceRequestHandler()
    }
}