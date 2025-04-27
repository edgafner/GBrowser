package com.github.gbrowser.ui.search.impl

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import com.github.gbrowser.ui.toolwindow.gbrowser.getHBookmarkItemsWidthValue
import com.github.gbrowser.ui.toolwindow.gbrowser.getHistoryItemsWidthValue
import com.github.gbrowser.ui.toolwindow.gbrowser.getSuggestionItems
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBList
import java.awt.event.KeyEvent
import javax.swing.ListSelectionModel


class GBrowserSearchTextField(project: Project, private val delegate: GBrowserToolWindowActionBarDelegate) : SearchTextField(false, true, "Search or type a URL") {

  private val settings = project.service<GBrowserService>()
  private val favIconLoader: CachingFavIconLoader = service()
  private var popupList: JBList<GBrowserSearchPopUpItem>? = null
  private var popup: JBPopup? = null
  private var isSearchHighlightEnabled: Boolean = settings.isSuggestionSearchHighlighted
  private var isSearchHostHidden: Boolean = settings.isProtocolHidden
  private var isSearchHostHighlighted: Boolean = settings.isHostHighlight
  private val items = LinkedHashSet<GBrowserSearchPopUpItem>()
  private var originalText: String? = null

  init {
    textEditor.putClientProperty("JTextField.Search.Icon", GBrowserIcons.GBROWSER_LOGO)
    updateData()
    settings.addListener { state: GBrowserService.SettingsState ->
      isSearchHighlightEnabled = state.isSuggestionSearchHighlighted
      isSearchHostHidden = state.isProtocolHidden
      isSearchHostHighlighted = state.isHostHighlight
      items.clear()
      updateData()
    }


  }

  private fun updateData() {
    items.clear()
    val currentText = text.trim()
    if (currentText.isNotEmpty() && currentText.length < 30) {
      items.addAll(getHistoryItemsWidthValue(currentText, settings.history, favIconLoader, settings.isFavIconEnabled))
      items.addAll(getHBookmarkItemsWidthValue(currentText, settings.bookmarks))
      if (settings.isSuggestionSearchEnabled) {
        items.addAll(getSuggestionItems(currentText))
      }
    }
  }

  override fun preprocessEventForTextField(event: KeyEvent): Boolean { //when (e.keyCode) {
    if (event.keyCode in MINOR_KEYS) {
      return super.preprocessEventForTextField(event)
    }
    return when (event.id) {
      KeyEvent.KEY_PRESSED -> {
        when (event.keyCode) {
          KeyEvent.VK_DOWN -> handleKeyDownPress()
          KeyEvent.VK_UP -> handleKeyUpPress()
          else -> {
            hidePopup()
            super.preprocessEventForTextField(event)
          }
        }
      }
      KeyEvent.KEY_RELEASED -> {
        when (event.keyCode) {
          KeyEvent.VK_ESCAPE -> handleKeyEscPress()
          KeyEvent.VK_ENTER -> handleKeyEnterPress()
          else -> updatePopupContent()
        }
      }
      else -> super.preprocessEventForTextField(event)
    }
  }

  override fun onFocusLost() {
    super.onFocusLost() // Additional logic when focus is lost
    if (text.isEmpty()) {
      text = originalText ?: ""
    }
    delegate.onSearchFocusLost()
    hidePopup()
  }

  override fun onFocusGained() {
    super.onFocusGained()
    originalText = text
    delegate.onSearchFocus()
  }

  // Override showPopup() if needed to customize popup content
  override fun showPopup() {
    if ((popup == null || popup?.isVisible == false) && items.isNotEmpty()) {
      createPopup()
    }
  }

  override fun hidePopup() {
    popup?.cancel()
  }

  private fun handleKeyDownPress(): Boolean {
    if (popupList != null) {
      popupList?.let { list ->
        val size = list.model.size
        if (size > 0) {
          val selectedIndex = (list.selectedIndex + 1).coerceAtMost(size - 1)
          list.selectedIndex = selectedIndex
          list.ensureIndexIsVisible(selectedIndex)
        }
      }
    } else {
      showPopup()
    }
    return true
  }

  private fun handleKeyUpPress(): Boolean {
    popupList?.let { list ->
      if (list.model.size > 0) {
        val selectedIndex = (list.selectedIndex - 1).coerceAtLeast(0)
        list.selectedIndex = selectedIndex
        list.ensureIndexIsVisible(selectedIndex)
      }
    }
    return true
  }

  private fun handleKeyEscPress(): Boolean {
    text = originalText ?: ""
    hidePopup()
    return true
  }

  private fun handleKeyEnterPress(): Boolean {
    val selectedUrl = popupList?.selectedValue?.url
    if (!selectedUrl.isNullOrBlank()) { // If there's a selected item in the popup, use its URL
      text = selectedUrl
      delegate.onSearchEnter(selectedUrl)
      hidePopup()
      // Force blur the text field to move focus away from it
      transferFocusBackward()
    } else if (text.trim().isNotEmpty()) { // If no item is selected, but there's a text in the search field
      // We don't need to add to history here as it's handled in GBrowserToolWindowBrowser.setHistoryItem()
      delegate.onSearchEnter(text.trim())
      hidePopup()
      // Force blur the text field to move focus away from it
      transferFocusBackward()
    } else {
      hidePopup()
    }
    return true
  }

  private fun updatePopupContent(): Boolean {
    val currentText = text
    if (currentText.isNotBlank() && currentText.length < 30) { // Filter and add history, favorites, and suggested items
      updateData()
      showPopup()
    } else {
      hidePopup()
    }
    return true
  }

  private fun createPopup() {
    if (isShowing) {
      val currentText = text
      val filteredItems = items.filter { it.matchesText(currentText) }
      popupList = JBList(filteredItems.take(10))  // Update 'popupList' with filtered items
      popupList?.let { list ->
        val popupBuilder = PopupChooserBuilder(list).setRenderer(GBrowserSearchPopCellRenderer(isSearchHighlightEnabled)).setCloseOnEnter(
          true
        ).setAutoSelectIfEmpty(false).setRequestFocus(false).setSelectionMode(
          ListSelectionModel.SINGLE_SELECTION
        ).setCancelKeyEnabled(true)

        popup = popupBuilder.createPopup().apply {
          showUnderneathOf(this@GBrowserSearchTextField)
        }
      }
    }
  }

  companion object {
    private val MINOR_KEYS = listOf(
      KeyEvent.VK_ALT,
      KeyEvent.VK_OPEN_BRACKET,
      KeyEvent.VK_CLOSE_BRACKET,
      KeyEvent.VK_TAB,
      KeyEvent.VK_SHIFT,
      KeyEvent.VK_META,
      KeyEvent.VK_CONTROL,
      KeyEvent.VK_LEFT,
      KeyEvent.VK_RIGHT,
    )

  }
}