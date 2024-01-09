package com.github.gbrowser.ui.toolwindow.gbrowser

interface GBrowserToolWindowActionBarDelegate {

  fun onToolBarIcon(text: String)

  fun onSearchEnter(text: String)

  fun onSearchFocus()

  fun onSearchFocusLost()

  fun onAddressChange(url: String)

  fun onTitleChange(title: String)


}