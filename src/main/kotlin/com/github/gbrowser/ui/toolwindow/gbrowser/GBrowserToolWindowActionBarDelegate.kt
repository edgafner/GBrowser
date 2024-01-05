package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem

interface GBrowserToolWindowActionBarDelegate {

  fun onSearchEnter(text: String)

  fun onSearchFocus()

  fun onSearchFocusLost()

  fun onKeyReleased(text: String, popupItems: (List<GBrowserSearchPopUpItem>?) -> Unit)
}