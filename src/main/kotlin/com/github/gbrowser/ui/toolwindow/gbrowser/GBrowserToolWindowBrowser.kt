package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.settings.dao.GBrowserHistory
import com.github.gbrowser.ui.gcef.GBrowserCefDevToolsListener
import com.github.gbrowser.ui.gcef.GBrowserCefDisplayChangeDelegate
import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItemImpl
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.util.application
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.MessageBusConnection

class GBrowserToolWindowBrowser(private val toolWindow: ToolWindow) : SimpleToolWindowPanel(true, true), Disposable,
                                                                      GBrowserToolWindowActionBarDelegate, GBrowserCefDisplayChangeDelegate,
                                                                      GBrowserCefDevToolsListener {
  private val settings: GBrowserService = GBrowserService.instance()
  private var toolBar: GBrowserToolWindowActionBar = GBrowserToolWindowActionBar(this)
  private var currentUrl: String = settings.defaultUrl
  private var currentTitle: String = ""
  private var zoomLevel: Double = 0.0
  private var browser: GCefBrowser = GCefBrowser(toolWindow.project, currentUrl, null, null)
  private val devTools = GCefBrowser(toolWindow.project, null, browser.client, browser.devTools, browser.id)
  private val favIconLoader: CachingFavIconLoader = service()
  private val bus: MessageBus = ApplicationManager.getApplication().messageBus
  private val settingsConnection: MessageBusConnection = bus.connect()
  private var isSearchFocused: Boolean = false

  init {
    setToolbar(toolBar.component)
    setSearchText(currentUrl)
    setupBrowser()
    addSettingsListener()
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
    toolBar.dispose()
    removeSettingsListener()
  }

  private fun addSettingsListener() {
    settings.addListener { state: GBrowserService.SettingsState ->
      state.let {
        toolBar.search?.let {
          it.isHostHighlighted = state.isHostHighlight
          it.isHostHidden = state.isProtocolHidden
          it.setSearchHighlighted(state.isSuggestionSearchHighlighted)
        }

      }
    }
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
    toolBar.component.isVisible = isVisible
  }

  fun isToolBarVisible(): Boolean {
    return toolBar.component.isVisible
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

  private fun setSearchText(text: String) {
    toolBar.search?.setText(text)
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
    val canSuggestion = GBrowserUtil.isValidBrowserURL(text) && settings.isSuggestionSearchEnabled
    val url = if (canSuggestion) "https://www.google.com/search?q=$text" else text
    loadUrl(url)
  }


  override fun onSearchFocus() {
    isSearchFocused = true
  }


  override fun onSearchFocusLost() {
    isSearchFocused = false
  }

  override fun onKeyReleased(text: String,
                             popupItems: (List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>) -> Unit) {
    val historyItems = mutableListOf<GBrowserSearchPopUpItemImpl>()

    val history: MutableSet<GBrowserHistory> = settings.history
    historyItems.addAll(getHistoryItemsWidthValue(text, history, favIconLoader, settings.isFavIconEnabled))

    val bookMarksItems = mutableListOf<GBrowserSearchPopUpItemImpl>()
    val bookmarks: MutableSet<GBrowserBookmark> = settings.bookmarks
    bookMarksItems.addAll(getHBookmarkItemsWidthValue(text, bookmarks))

    val suggestedItems = mutableListOf<GBrowserSearchPopUpItemImpl>()
    val isSuggestionEnabled: Boolean = settings.isSuggestionSearchEnabled
    if (isSuggestionEnabled) {
      suggestedItems.addAll(getSuggestionItems(text))
    }

    popupItems.invoke(historyItems, bookMarksItems, suggestedItems)
  }

  override fun onDisposeDevtools() {
    browser.disposeDevTools()
  }

  override fun onAddressChange(url: String) {
    if (!isSearchFocused) {
      setSearchText(url)
    }

    setTabIcon(url)
    setHistoryItem()
    setCurrentUrl(url)

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
  }


}
