package com.github.gib.ui.toolwindow

import com.github.gib.ui.toolwindow.base.GBrowserTab
import org.jetbrains.annotations.NonNls

sealed interface GBrowserToolWindowTab : GBrowserTab {

  data class NewBrowserTab(val prId: String) : GBrowserToolWindowTab {
    override val id: @NonNls String = prId
    override val reuseTabOnRequest: Boolean = false
  }

}