package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.settings.dao.GBrowserHistory
import com.github.gbrowser.ui.gcef.GBrowserCefDevToolsListener
import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.util.application
import com.intellij.util.messages.MessageBus
import com.intellij.util.messages.MessageBusConnection
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.SwingUtilities

class GBrowserToolWindowBrowser(private val toolWindow: ToolWindow) : SimpleToolWindowPanel(true, true), Disposable, GBrowserToolWindowActionBarDelegate,
                                                                      GBrowserCefDevToolsListener {
  private val settings: GBrowserService = toolWindow.project.service<GBrowserService>()
  private var currentUrl: String = settings.defaultUrl
  private var gBrowserToolBar: GBrowserToolWindowActionBar = GBrowserToolWindowActionBar(toolWindow.project, this)
  private var currentTitle: String = ""
  private var zoomLevel: Double = 0.0
  private var gbrowser: GCefBrowser = GCefBrowser(toolWindow.project, currentUrl, null, null)
  private val devTools = GCefBrowser(toolWindow.project, null, gbrowser.client, gbrowser.devTools, gbrowser.id)
  private val favIconLoader: CachingFavIconLoader = service()
  private val bus: MessageBus = ApplicationManager.getApplication().messageBus
  private val settingsConnection: MessageBusConnection = bus.connect()
  private var isSearchFocused: Boolean = false

  init {
    toolbar = gBrowserToolBar.mainToolBarComponent
    gBrowserToolBar.search.text = currentTitle
    setupBrowser()
    setupResizeListener()
  }

  private fun setupBrowser() {
    gbrowser.addDisplayHandler(this)
    gbrowser.addLifeSpanHandler(toolWindow)
    gbrowser.addRequestHandler(GBrowserCefRequestHandler(toolWindow.project, null))
    setContent(gbrowser.component)
  }

  private fun setupResizeListener() {
    // Add component listener to handle resize events
    addComponentListener(object : ComponentAdapter() {
      override fun componentResized(e: ComponentEvent?) {
        // Force browser component to update its size
        SwingUtilities.invokeLater {
          gbrowser.forceResize()
          // Also revalidate the entire panel
          revalidate()
          repaint()
        }
      }

      override fun componentShown(e: ComponentEvent?) {
        // Ensure browser is properly sized when shown
        SwingUtilities.invokeLater {
          gbrowser.forceResize()
          gbrowser.setVisibility(true)
        }
      }
    })

    // Also listen to tool window state changes
    toolWindow.addContentManagerListener(object : com.intellij.ui.content.ContentManagerListener {
      override fun contentAdded(event: com.intellij.ui.content.ContentManagerEvent) {
        if (event.content.component == this@GBrowserToolWindowBrowser) {
          SwingUtilities.invokeLater {
            gbrowser.forceResize()
          }
        }
      }
    })
  }

  fun getCurrentUrl(): String = currentUrl

  private fun setCurrentUrl(url: String) {
    currentUrl = url
  }

  fun getCurrentTitle(): String = currentTitle

  private fun setCurrentTitle(title: String) {
    currentTitle = title
  }

  fun getBrowser(): GCefBrowser = gbrowser

  fun getDevToolsBrowser(): GCefBrowser = devTools


  override fun dispose() {
    gbrowser.dispose()
    gBrowserToolBar.dispose()
    removeSettingsListener()
  }


  fun reload() {
    gbrowser.cefBrowser.reloadIgnoreCache()
  }


  // Example of a method conversion:
  fun loadUrl(url: String) {
    if (!gbrowser.cefBrowser.isLoading) {
      gbrowser.cefBrowser.loadURL(url)
    } else {
      gbrowser.cefBrowser.stopLoad()
      gbrowser.cefBrowser.loadURL(url)
    }
  }

  fun stopLoad() {
    gbrowser.cefBrowser.stopLoad()
  }


  fun canGoBack(): Boolean {
    return gbrowser.cefBrowser.canGoBack()
  }

  fun canGoForward(): Boolean {
    return gbrowser.cefBrowser.canGoForward()
  }

  fun goBack() {
    gbrowser.cefBrowser.goBack()
  }

  fun goForward() {
    gbrowser.cefBrowser.goForward()
  }

  fun zoomIn() {
    zoomLevel += 1.0
    setZoom(zoomLevel)
  }

  fun zoomOut() {
    zoomLevel -= 1.0
    setZoom(zoomLevel)
  }

  fun zoomReset() {
    setZoom(0.0)
  }

  private fun setZoom(level: Double) {
    gbrowser.cefBrowser.zoomLevel = level
    zoomLevel = level
  }

  fun hasContent(): Boolean {
    val url = gbrowser.cefBrowser.url ?: return false
    return url.isNotEmpty()
  }


  fun deleteCookies() {
    gbrowser.deleteCookies()
  }


  fun setToolBarVisible(isVisible: Boolean) {
    gBrowserToolBar.mainToolBarComponent.isVisible = isVisible
  }

  fun isToolBarVisible(): Boolean {
    return gBrowserToolBar.mainToolBarComponent.isVisible
  }


  private fun setFocusOnBrowserUI() {
    gbrowser.cefBrowser.uiComponent.requestFocus()
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
    gbrowser.disposeDevTools()
  }

  override fun onAddressChange(url: String) {
    if (!isSearchFocused) {
      setFocusOnBrowserUI()
    }
    setTabIcon(url)
    setHistoryItem()
    setCurrentUrl(url)

    gbrowser.setVisibility(true)

    // Force UI update
    invalidate()
    validate()
    repaint()

    // Update search text
    SwingUtilities.invokeLater {
      gBrowserToolBar.search.text = url
    }

    // Load favicon
    favIconLoader.loadFavIcon(url.trim(), targetSize = 13).thenAccept {
      gBrowserToolBar.search.textEditor.putClientProperty(
        "JTextField.Search.Icon", it ?: GBrowserIcons.GBROWSER_LOGO
      )
    }

    // Extra visibility check after a short delay (helps with unpinned mode)
    SwingUtilities.invokeLater {
      ApplicationManager.getApplication().executeOnPooledThread {
        try {
          Thread.sleep(200)
          SwingUtilities.invokeLater { // Re-check and enforce visibility
            gbrowser.setVisibility(true)
            invalidate()
            validate()
            repaint()
          }
        } catch (e: InterruptedException) {
          thisLogger().warn("GBrowserToolWindowBrowser: Interrupted thread", e)
          Thread.currentThread().interrupt()
        }
      }
    }
  }

  override fun onTitleChange(title: String) {
    setTabName(title)
    setCurrentTitle(title)
    gbrowser.setVisibility(true)
    gbrowser.notifyTitleChanged(title)
    devTools.notifyTitleChanged(title)
  }


}
