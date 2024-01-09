package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.dao.GBrowserHistory
import com.github.gbrowser.ui.gcef.GBrowserCefDevToolsListener
import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.util.application
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.MessageBusConnection

class GBrowserToolWindowBrowser(private val toolWindow: ToolWindow) : SimpleToolWindowPanel(true, true), Disposable,
                                                                      GBrowserToolWindowActionBarDelegate, GBrowserCefDevToolsListener {
  private val settings: GBrowserService = GBrowserService.instance()
  private var currentUrl: String = settings.defaultUrl
  private var gBrowserToolBar: GBrowserToolWindowActionBar = GBrowserToolWindowActionBar(this)
  private var currentTitle: String = ""
  private var zoomLevel: Double = 0.0
  private var browser: GCefBrowser = GCefBrowser(toolWindow.project, currentUrl, null, null)
  private val devTools = GCefBrowser(toolWindow.project, null, browser.client, browser.devTools, browser.id)
  private val favIconLoader: CachingFavIconLoader = service()
  private val bus: MessageBus = ApplicationManager.getApplication().messageBus
  private val settingsConnection: MessageBusConnection = bus.connect()
  private var isSearchFocused: Boolean = false

  init {
    toolbar = gBrowserToolBar.mainToolBarComponent
    gBrowserToolBar.search.text = currentTitle
    setupBrowser()
  }

  private fun setupBrowser() {
    browser.addDisplayHandler(this)
    browser.addLifeSpanHandler(toolWindow)
    browser.addRequestHandler(GBrowserCefRequestHandler(null))
    setContent(browser.component)
  }

  fun getCurrentUrl(): String = currentUrl

  private fun setCurrentUrl(url: String) {
    currentUrl = url
  }

  fun getCurrentTitle(): String = currentTitle

  private fun setCurrentTitle(title: String) {
    currentTitle = title
  }

  fun getBrowser(): GCefBrowser = browser

  fun getDevToolsBrowser(): GCefBrowser = devTools

  override fun dispose() {
    browser.dispose()
    gBrowserToolBar.dispose()
    removeSettingsListener()
  }

  // Other methods (reload, loadUrl, stopLoad, loadDefaultUrl, etc.) would be similarly converted.

  fun reload() {
    browser.cefBrowser.reloadIgnoreCache()
  }


  // Example of a method conversion:
  fun loadUrl(url: String) {
    browser.loadURL(url)
  }

  fun stopLoad() {
    browser.cefBrowser.stopLoad()
  }

  fun loadDefaultUrl() {
    browser.loadURL(settings.defaultUrl)
  }


  fun canGoBack(): Boolean {
    return browser.cefBrowser.canGoBack()
  }

  fun canGoForward(): Boolean {
    return browser.cefBrowser.canGoForward()
  }

  fun goBack() {
    browser.cefBrowser.goBack()
  }

  fun goForward() {
    browser.cefBrowser.goForward()
  }

  fun zoomIn() {
    zoomLevel += 1.0
    setZoom(zoomLevel)
  }

  fun zoomOut() {
    zoomLevel += -1.0
    setZoom(zoomLevel)
  }

  fun zoomReset() {
    setZoom(0.0)
  }

  private fun setZoom(level: Double) {
    browser.cefBrowser.zoomLevel = level
    zoomLevel = level
  }

  fun hasContent(): Boolean {
    val url = browser.cefBrowser.url ?: return false
    return url.isNotEmpty()
  }


  fun deleteCookies() {
    browser.deleteCookies()
  }


  fun setToolBarVisible(isVisible: Boolean) {
    gBrowserToolBar.mainToolBarComponent.isVisible = isVisible
  }

  fun isToolBarVisible(): Boolean {
    return gBrowserToolBar.mainToolBarComponent.isVisible
  }


  private fun setFocusOnBrowserUI() {
    browser.cefBrowser.uiComponent.requestFocus()
  }


  private fun setTabName(name: String) {
    val tabComponent = toolWindow.contentManager.getContent(this)
    if (tabComponent != null) {
      application.invokeLater {
        val tabName = if (name.length > 11) name.take(11).plus("...").trim() else name.trim()
        tabComponent.displayName = tabName
      }
    }
  }

  private fun setTabIcon(url: String) {
    val builderContent = toolWindow.contentManager.getContent(this)
    if (builderContent != null) {
      favIconLoader.loadFavIcon(url).thenAccept { icon ->
        icon?.let { builderContent.icon = it }
      }

    }
  }

  private fun setHistoryItem() {
    if (currentUrl.trim().isNotEmpty()) {
      if (settings.isHistoryEnabled) {
        settings.addHistory(GBrowserHistory(currentTitle, currentUrl))
      }
    }
  }

  private fun removeSettingsListener() {
    settingsConnection.disconnect()
  }

  override fun onToolBarIcon(text: String) {
    loadUrl(text)
  }

  override fun onSearchEnter(text: String) {
    val canSuggestion = !GBrowserUtil.isValidBrowserURL(text) && settings.isSuggestionSearchEnabled
    val url = if (canSuggestion) "https://www.google.com/search?q=$text" else text
    loadUrl(url)
  }


  override fun onSearchFocus() {
    isSearchFocused = true
  }


  override fun onSearchFocusLost() {
    isSearchFocused = false
  }


  override fun onDisposeDevtools() {
    browser.disposeDevTools()
  }

  override fun onAddressChange(url: String) {
    if (!isSearchFocused) {
      setFocusOnBrowserUI()
    }
    setTabIcon(url)
    setHistoryItem()
    setCurrentUrl(url)
    browser.setVisibility(true)
    gBrowserToolBar.search.text = url
    application.invokeLater {
      UISettings.getInstance().fireUISettingsChanged()
    }
  }

  override fun onTitleChange(title: String) {
    if (!isSearchFocused) {
      setFocusOnBrowserUI()
    }
    setTabName(title)
    setCurrentTitle(title)
    browser.setVisibility(true)
    browser.notifyTitleChanged(title)
    devTools.notifyTitleChanged(title)
    application.invokeLater {
      UISettings.getInstance().fireUISettingsChanged()
    }
  }


}
