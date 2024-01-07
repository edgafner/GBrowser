package com.github.gbrowser.ui.gcef.impl

import com.github.gbrowser.services.providers.CachingWebPageTitleLoader
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.ToolWindow
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLifeSpanHandlerAdapter

class GBrowserCefLifeSpanDelegate(private val toolWindow: ToolWindow) : CefLifeSpanHandlerAdapter() {

  private val favIconLoader: CachingWebPageTitleLoader = service()

  override fun onBeforePopup(browser: CefBrowser?, frame: CefFrame?, targetUrl: String?, targetFrameName: String?): Boolean {
    if (GBrowserService.instance().navigateInNewTab || targetUrl == null) {
      return false
    }
    toolWindow.let { tw ->
      favIconLoader.getTitleOfWebPage(targetUrl).thenAccept {
        GBrowserToolWindowUtil.createContentTab(tw, targetUrl, it)
      }
    }

    return true
  }
}