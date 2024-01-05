package com.github.gbrowser.ui.gcef

import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.gcef.impl.GBrowserCefDisplayChangeHandler
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefLoadHandler
import org.cef.network.CefCookieManager
import java.util.*
import org.cef.browser.CefBrowser as OrgCefBrowser

@Suppress("MemberVisibilityCanBePrivate")
class GCefBrowser(url: String, client: JBCefClient? = null, browser: OrgCefBrowser? = null) : JBCefBrowser(createBuilder().apply {
  setOffScreenRendering(false)
  setEnableOpenDevToolsMenuItem(true)
  setCefBrowser(browser)
  setClient(client)
  setUrl(url)
}) {
  val id: String
    get() {
      return UUID.randomUUID().toString()
    }

  val devTools: OrgCefBrowser
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
      override fun onBeforeContextMenu(browser: CefBrowser, frame: CefFrame, params: CefContextMenuParams, model: CefMenuModel) {
        model.addItem(28501, "Add to Bookmarks")
        model.addItem(28502, "Add to Quick Access")
        super.onBeforeContextMenu(browser, frame, params, model)
      }

      override fun onContextMenuCommand(browser: CefBrowser,
                                        frame: CefFrame,
                                        params: CefContextMenuParams,
                                        commandId: Int,
                                        eventFlags: Int): Boolean {
        if (commandId == 28501) {
          LOG.info("Add to Bookmarks was invoked: ${browser.url}")
          addToBookmarks(browser)
          return true
        } //if (commandId == 28502) {
        //  LOG.info("Add to Quick Access was invoked: ${browser.url}")
        //  addToBookmarksAndToolBar(browser)
        //  return true
        //}
        return super.onContextMenuCommand(browser, frame, params, commandId, eventFlags)
      }

      private fun addToBookmarks(browser: CefBrowser) {
        GBrowserSetting.instance().addBookmarks(GBrowserBookmark(browser.url, GBrowserUtil.getTitleOfWebPage(browser.url)))
      }
    }
  }

  fun setVisibility(isVisible: Boolean) {
    this.component.isVisible = isVisible
  }


  fun deleteCookies() {
    val manager = CefCookieManager.getGlobalManager()
    manager.deleteCookies(null, null)
  }

  fun addTitleChangeListener(delegate: GBrowserCefBrowserTitleDelegate) {
    this.titleChangeDelegate = delegate
  }

  fun removeTitleChangeListener() {
    this.titleChangeDelegate = null
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

  companion object {
    val LOG = logger<GCefBrowser>()
  }

}

//
//class GBCefBrowser(url: String?) : JBCefBrowser(
//  createBuilder().setOffScreenRendering(false).setEnableOpenDevToolsMenuItem(true).setUrl(url)) {
//
//  private var myDevtoolsFrame: JDialog? = null
//
//  override fun openDevtools() {
//    LOG.info("Open DevTools was invoked")
//    if (myDevtoolsFrame != null) {
//      myDevtoolsFrame!!.toFront()
//      return
//    }
//    val comp: Component = component
//    val ancestor = (SwingUtilities.getWindowAncestor(comp)) ?: return
//    val bounds = ancestor.graphicsConfiguration.bounds
//    myDevtoolsFrame = JDialog(ancestor)
//    myDevtoolsFrame!!.title = "JCEF DevTools"
//    myDevtoolsFrame!!.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
//    myDevtoolsFrame!!.setBounds(bounds.width / 4 + 100, bounds.height / 4 + 100, bounds.width / 2, bounds.height / 2)
//    myDevtoolsFrame!!.layout = BorderLayout()
//    val devTools = createBuilder().setCefBrowser(cefBrowser.devTools).setClient(jbCefClient).build()
//    myDevtoolsFrame!!.add(devTools.component, BorderLayout.CENTER)
//    myDevtoolsFrame!!.addWindowListener(object : WindowAdapter() {
//      override fun windowClosed(e: WindowEvent) {
//        myDevtoolsFrame = null
//
//      }
//    })
//    myDevtoolsFrame!!.isVisible = true
//  }
//
//}
