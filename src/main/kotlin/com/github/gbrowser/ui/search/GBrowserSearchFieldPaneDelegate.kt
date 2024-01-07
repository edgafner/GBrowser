package com.github.gbrowser.ui.search

interface GBrowserSearchFieldPaneDelegate {

  fun onCancel()

  fun onDoubleClick()

  fun onEnter(url: String)

  fun onFocus()

  fun onFocusLost()

  fun onKeyReleased(text: String,
                    popupItems: (List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>) -> Unit)

  fun onMouseEntered()

  fun onMouseExited()

  fun onSelect(item: GBrowserSearchPopUpItemImpl)
}