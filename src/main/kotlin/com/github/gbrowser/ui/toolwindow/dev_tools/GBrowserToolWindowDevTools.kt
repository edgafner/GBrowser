package com.github.gbrowser.ui.toolwindow.dev_tools


import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.ui.gcef.GBrowserCefBrowserTitleDelegate
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.util.application
import org.cef.CefClient
import java.awt.Component
import java.util.*

class GBrowserToolWindowDevTools(private var toolWindow: ToolWindow, var browser: GCefBrowser) : SimpleToolWindowPanel(true, true),
                                                                                                 Disposable,
                                                                                                 GBrowserCefBrowserTitleDelegate {

  private val uiComponent: Component = browser.cefBrowser.uiComponent
  private val client: CefClient = browser.cefBrowser.client

  init {
    browser.setVisibility(false)
    browser.addTitleChangeListener(this)
    createFocusListener()
    setContent(browser.component)
    Timer().schedule(object : TimerTask() {
      override fun run() {
        browser.setVisibility(true)
        uiComponent.requestFocus()
      }
    }, 300L)
  }

  private fun createFocusListener() {
    uiComponent.addFocusListener(GBrowserDevToolsFocusListener(client))
  }

  private fun removeFocusListener() {
    uiComponent.removeFocusListener(GBrowserDevToolsFocusListener(client))
  }

  override fun dispose() {
    browser.removeTitleChangeListener()
    browser.disposeDevTools()
    removeFocusListener()
  }

  override fun onChangeTitle(title: String?) {
    if (title == null) return
    toolWindow.contentManager.getContent(this)?.let {
      application.invokeLater {
        val tabName = if (title.length >= 17) title.take(17) + "..." else title
        it.displayName = tabName
      }
    }
  }
}

