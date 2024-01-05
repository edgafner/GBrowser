package com.github.gbrowser.ui.gcef

interface GBrowserCefDisplayChangeDelegate {

  fun onAddressChange(url: String)

  fun onTitleChange(title: String)
}