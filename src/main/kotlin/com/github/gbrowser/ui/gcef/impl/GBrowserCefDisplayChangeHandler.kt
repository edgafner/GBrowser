package com.github.gbrowser.ui.gcef.impl


import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import com.intellij.openapi.application.ApplicationManager
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter

/**
 * JCEF calls these callbacks on a CEF thread, not on the EDT. The delegate updates Swing state
 * (validate/repaint, tool window content, favicon), so it is always re-dispatched to the EDT.
 * Running it inline blocks the CEF thread on the AWT tree lock while the EDT waits on a
 * synchronous CEF call, freezing the whole IDE (issue #417).
 */
class GBrowserCefDisplayChangeHandler(
  private val delegate: GBrowserToolWindowActionBarDelegate,
  private val edtDispatcher: (Runnable) -> Unit = { ApplicationManager.getApplication().invokeLater(it) },
) : CefDisplayHandlerAdapter() {

  override fun onAddressChange(browser: CefBrowser?, frame: CefFrame?, url: String?) {
    val isValidURL = !url.isNullOrBlank() && !url.startsWith("devtools") && !url.startsWith("file:///jbcefbrowser")
    if (isValidURL) {
      edtDispatcher(Runnable { delegate.onAddressChange(url) })
    }
    super.onAddressChange(browser, frame, url)
  }

  override fun onTitleChange(browser: CefBrowser?, title: String?) {
    val isValidTitle = !title.isNullOrBlank() && !title.startsWith("DevTools")
    if (isValidTitle) {
      edtDispatcher(Runnable { delegate.onTitleChange(title) })
    }
    super.onTitleChange(browser, title)
  }
}
