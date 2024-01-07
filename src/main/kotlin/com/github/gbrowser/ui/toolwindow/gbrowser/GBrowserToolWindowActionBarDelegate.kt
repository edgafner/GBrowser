package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.ui.search.GBrowserSearchPopUpItemImpl

interface GBrowserToolWindowActionBarDelegate {

  fun onToolBarIcon(text: String)

  fun onSearchEnter(text: String)

  fun onSearchFocus()

  fun onSearchFocusLost()

  fun onKeyReleased(text: String, popupItems: (List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>) -> Unit)
}