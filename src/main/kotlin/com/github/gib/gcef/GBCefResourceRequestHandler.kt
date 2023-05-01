package com.github.gib.gcef

import com.github.gib.services.GivServiceSettings
import org.cef.handler.CefResourceRequestHandlerAdapter

class GBCefResourceRequestHandler : CefResourceRequestHandlerAdapter() {
    override fun onBeforeResourceLoad(browser: org.cef.browser.CefBrowser?, frame: org.cef.browser.CefFrame?, request: org.cef.network.CefRequest?): Boolean {
        if(GivServiceSettings.instance().getLastSaveOverrideUserAgent()){
            request?.setHeaderByName("User-Agent", GivServiceSettings.instance().getLastSaveUserAgent(), true)
        }
        return super.onBeforeResourceLoad(browser, frame, request)
    }
}