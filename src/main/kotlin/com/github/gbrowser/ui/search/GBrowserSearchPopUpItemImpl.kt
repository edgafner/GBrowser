package com.github.gbrowser.ui.search

import javax.swing.Icon

interface GBrowserSearchPopUpItem
data class GBrowserSearchPopUpItemImpl(val highlight: String,
                                       var icon: Icon?,
                                       var info: String?,
                                       val isURLVisible: Boolean,
                                       var name: String,
                                       var url: String) : GBrowserSearchPopUpItem

class GBrowserSearchPopUpItemSeparator : GBrowserSearchPopUpItem