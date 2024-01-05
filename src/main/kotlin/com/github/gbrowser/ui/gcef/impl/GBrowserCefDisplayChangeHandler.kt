package com.github.gbrowser.ui.gcef.impl


import com.github.gbrowser.ui.gcef.GBrowserCefDisplayChangeDelegate
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter

class GBrowserCefDisplayChangeHandler(private val delegate: GBrowserCefDisplayChangeDelegate) : CefDisplayHandlerAdapter() {

  override fun onAddressChange(browser: CefBrowser?, frame: CefFrame?, url: String?) {
    val isValidURL = !url.isNullOrBlank() && !url.startsWith("devtools") && !url.startsWith("file:///jbcefbrowser")
    if (isValidURL) {
      delegate.onAddressChange(url!!)
    }
    super.onAddressChange(browser, frame, url)
  }

  override fun onTitleChange(browser: CefBrowser?, title: String?) {
    val isValidTitle = !title.isNullOrBlank() && !title.startsWith("DevTools")
    if (isValidTitle) {
      delegate.onTitleChange(title!!)
    }
    super.onTitleChange(browser, title)
  }
}
