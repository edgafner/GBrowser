package com.github.gib.ui.toolwindow.base

/**
 * Represents the state of review toolwindow tabs
 *
 * @param T tab type
 * @param TVM tab view model
 */
data class GBrowserToolwindowTabs<T : GBrowserTab, TVM : GBrowserTabViewModel>(
  val tabs: Map<T, TVM>,
  val selectedTab: T?
)
