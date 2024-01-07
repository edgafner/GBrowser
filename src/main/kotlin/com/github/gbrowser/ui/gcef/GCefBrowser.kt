package com.github.gbrowser.ui.gcef

import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.providers.CachingWebPageTitleLoader
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.gcef.impl.GBrowserCefDisplayChangeHandler
import com.github.gbrowser.ui.gcef.impl.GBrowserCefLifeSpanDelegate
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevToolsFactory
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefClient
import com.intellij.util.application
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.callback.CefMenuModel.MenuId
import org.cef.handler.CefLoadHandler
import org.cef.network.CefCookieManager
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
class GCefBrowser(val project: Project,
                  url: String?,
                  client: JBCefClient? = null,
                  browser: CefBrowser? = null,
                  val id: String = UUID.randomUUID().toString()) : JBCefBrowser(createBuilder().apply {
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
        if (isProperty(JBCefBrowserBase.Properties.NO_CONTEXT_MENU)) {
          model.clear()
          return
        }

        model.addItem(MenuId.MENU_ID_USER_LAST, "Open DevTools")
        model.addItem(BOOKMARK_ADD, GBrowserBundle.message(
          "action.GBrowserBookmarkAddAction.text")) //model.addItem(26502, GBrowserBundle.message("action.GBrowserBookmarkAddAction.text")) //super.onBeforeContextMenu(browser, frame, params, model)
      }

      override fun onContextMenuCommand(browser: CefBrowser,
                                        frame: CefFrame,
                                        params: CefContextMenuParams,
                                        commandId: Int,
                                        eventFlags: Int): Boolean {
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
          GBrowserService.instance().addBookmarks(GBrowserBookmark(browser.url, it))
        }
      }

      private fun openDevtools() {
        val selectedBrowser = GBrowserToolWindowUtil.getSelectedBrowserPanel(project) ?: return
        val browser = selectedBrowser.getDevToolsBrowser()
        application.invokeLater {
          GBrowserToolWindowDevToolsFactory.Companion.createTab(project, browser, selectedBrowser.getCurrentTitle())
        }
      }
    }
  }


  fun setVisibility(isVisible: Boolean) {
    component.isVisible = isVisible
  }


  fun deleteCookies() {
    val manager = CefCookieManager.getGlobalManager()
    manager.deleteCookies(null, null)
  }

  fun addTitleChangeListener(delegate: GBrowserCefBrowserTitleDelegate) {
    titleChangeDelegate = delegate
  }

  fun removeTitleChangeListener() {
    titleChangeDelegate = null
  }

  fun notifyTitleChanged(title: String?) {
    titleChangeDelegate?.onChangeTitle(title)
  }

  fun addDevToolsListener(delegate: GBrowserCefDevToolsListener) {
    if (devToolsDelegates.none { it == delegate }) {
      devToolsDelegates.add(delegate)
    }
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


  fun addDisplayHandler(delegate: GBrowserCefDisplayChangeDelegate) {
    cefBrowser.client?.addDisplayHandler(GBrowserCefDisplayChangeHandler(delegate))
  }

  fun addLifeSpanHandler(toolWindow: ToolWindow) {
    cefBrowser.client?.addLifeSpanHandler(GBrowserCefLifeSpanDelegate(toolWindow))
  }

  fun removeDisplayHandler() {
    cefBrowser.client?.removeDisplayHandler()
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

    super.dispose()
  }
}