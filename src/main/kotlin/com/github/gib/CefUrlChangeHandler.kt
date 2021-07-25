package com.github.gib

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import java.util.function.Consumer

class CefUrlChangeHandler(private val onUrlChange: Consumer<String?>) : CefDisplayHandlerAdapter() {

    override fun onAddressChange(browser: CefBrowser?, frame: CefFrame?, url: String?) {
        if (url != null && url.startsWith("devtools")) {
            return
        }
        onUrlChange.accept(url)
    }


}