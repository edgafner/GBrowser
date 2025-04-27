package com.github.gbrowser.ui.gcef

import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.services.providers.CachingWebPageTitleLoader
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.gcef.impl.GBrowserCefDisplayChangeHandler
import com.github.gbrowser.ui.gcef.impl.GBrowserCefLifeSpanDelegate
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.callback.CefMenuModel.MenuId
import org.cef.handler.CefLoadHandler
import org.cef.network.CefCookieManager
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
class GCefBrowser(val project: Project, url: String?, client: JBCefClient? = null, browser: CefBrowser? = null, val id: String = UUID.randomUUID().toString()) :
  JBCefBrowser(createBuilder().apply {
    setOffScreenRendering(false)
    setEnableOpenDevToolsMenuItem(true)
    setCefBrowser(browser)
    setClient(client)
    setUrl(url)
  }) {

  private val favIconLoader: CachingWebPageTitleLoader = service()


  val devTools: CefBrowser
    get() {
      return cefBrowser.devTools
    }

  val client: JBCefClient
    get() {
      return jbCefClient
    }
  private var titleChangeDelegate: GBrowserCefBrowserTitleDelegate? = null
  private var devToolsDelegates: MutableList<GBrowserCefDevToolsListener> = mutableListOf()

  init {
    setProperty("JBCefBrowser.focusOnShow", true)
    setProperty("JBCefBrowser.focusOnNavigation", true)
    setErrorPage { errorCode, errorText, failedUrl ->
      if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) null
      else GBrowserErrorPage.create(errorCode, errorText, failedUrl)
    }
  }


  override fun createDefaultContextMenuHandler(): DefaultCefContextMenuHandler {
    return object : DefaultCefContextMenuHandler(true) {
      private val BOOKMARK_ADD: Int = 26501

      override fun onBeforeContextMenu(browser: CefBrowser, frame: CefFrame, params: CefContextMenuParams, model: CefMenuModel) {
        if (isProperty(Properties.NO_CONTEXT_MENU)) {
          model.clear()
          return
        }

        model.addItem(MenuId.MENU_ID_USER_LAST, "Open DevTools")
        model.addItem(
          BOOKMARK_ADD, GBrowserBundle.message(
            "action.GBrowserBookmarkAddAction.text"
          )
        )
      }

      override fun onContextMenuCommand(browser: CefBrowser, frame: CefFrame, params: CefContextMenuParams, commandId: Int, eventFlags: Int): Boolean {
        if (commandId == BOOKMARK_ADD) {
          addToBookmarks(browser)
          return true
        }
        if (commandId == MenuId.MENU_ID_USER_LAST) {
          openDevtools()
          return true

        } //  addToBookmarksAndToolBar(browser)
        //  return true
        return super.onContextMenuCommand(browser, frame, params, commandId, eventFlags)
      }

      private fun addToBookmarks(browser: CefBrowser) {
        favIconLoader.getTitleOfWebPage(browser.url).thenAccept {
          project.service<GBrowserService>().addBookmarks(GBrowserBookmark(browser.url, it))
        }
      }

    }
  }

  fun setVisibility(isVisible: Boolean) {
    component.isVisible = isVisible

    // Also ensure the browser UI component is visible
    // This addresses specific unpinned mode rendering issues
    cefBrowser.uiComponent?.isVisible = isVisible

    // Force a repaint if becoming visible
    if (isVisible) {
      component.invalidate()
      component.validate()
      component.repaint()

      // Also repaint the UI component
      cefBrowser.uiComponent?.invalidate()
      cefBrowser.uiComponent?.validate()
      cefBrowser.uiComponent?.repaint()

      // Ensure parent containers are refreshed too
      val parent = component.parent
      parent?.invalidate()
      parent?.validate()
      parent?.repaint()
    }
  }

  fun deleteCookies() {
    val manager = CefCookieManager.getGlobalManager()
    manager.deleteCookies(null, null)
  }

  fun notifyTitleChanged(title: String?) {
    titleChangeDelegate?.onChangeTitle(title)
  }

  fun removeDevToolsListener() {
    devToolsDelegates = mutableListOf()
  }

  fun disposeDevTools() {
    devTools.close(true)
    devToolsDelegates.forEach {
      it.onDisposeDevtools()

    }
    removeDevToolsListener()

  }

  fun addDisplayHandler(delegate: GBrowserToolWindowActionBarDelegate) {
    cefBrowser.client?.addDisplayHandler(GBrowserCefDisplayChangeHandler(delegate))
  }

  fun addLifeSpanHandler(toolWindow: ToolWindow) {
    cefBrowser.client?.addLifeSpanHandler(GBrowserCefLifeSpanDelegate(project, toolWindow))
  }

  fun removeDisplayHandler() {
    cefBrowser.client?.removeDisplayHandler()
  }

  fun removeLifeSpanHandler() {
    cefBrowser.client?.removeLifeSpanHandler()
  }

  fun addRequestHandler(handler: GBrowserCefRequestHandler) {
    cefBrowser.client?.removeRequestHandler()
    cefBrowser.client?.addRequestHandler(handler)
  }

  fun removeRequestHandler() {
    cefBrowser.client?.removeRequestHandler()
  }

  override fun dispose() {
    removeDisplayHandler()
    removeRequestHandler()
    devToolsDelegates.forEach {
      it.onDisposeDevtools()
    }
    removeDevToolsListener()
    removeLifeSpanHandler()

    super.dispose()
  }
}