package com.github.gib.gcef

import com.github.gib.services.GivServiceSettings
import org.cef.handler.CefResourceRequestHandlerAdapter

class GBCefResourceRequestHandler : CefResourceRequestHandlerAdapter() {
    override fun onBeforeResourceLoad(browser: org.cef.browser.CefBrowser?, frame: org.cef.browser.CefFrame?, request: org.cef.network.CefRequest?): Boolean {
        GivServiceSettings.instance().getHeadersOverwrite().forEach {
            if(request?.url?.matches(Regex(it.uriRegex)) == true){
                    request.setHeaderByName(it.header, it.value, it.overwrite)
            }
        }
        return super.onBeforeResourceLoad(browser, frame, request)
    }
}