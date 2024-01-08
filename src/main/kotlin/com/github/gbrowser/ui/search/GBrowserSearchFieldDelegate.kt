package com.github.gbrowser.ui.search

interface GBrowserSearchFieldDelegate {

  fun onEnter(url: String)

  fun onFocus()

  fun onFocusLost()

  fun onKeyReleased(text: String, popupItems: (List<GBrowserSearchPopUpItem>) -> Unit)
}