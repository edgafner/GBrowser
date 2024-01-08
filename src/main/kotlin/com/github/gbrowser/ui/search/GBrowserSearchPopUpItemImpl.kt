package com.github.gbrowser.ui.search

import javax.swing.Icon

data class GBrowserSearchPopUpItem(val highlight: String,
                                   var icon: Icon?,
                                   var info: String?,
                                   val isURLVisible: Boolean,
                                   var name: String,
                                   var url: String)
