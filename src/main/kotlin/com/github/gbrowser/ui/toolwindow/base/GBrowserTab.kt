package com.github.gbrowser.ui.toolwindow.base

import org.jetbrains.annotations.NonNls

/**
 * Represents a single closable tab in review toolwindow (e.g. review details tab).
 */
interface GBrowserTab {
  /**
   * Unique id used to distinguish tabs that can be reused,
   * so if tab with the same [id] is requested to be opened
   * it will be reused if [reuseTabOnRequest] is true
   * or closed and new one will be opened.
   */
  val id: @NonNls String

  /**
   * If true open requests will select opened tabs if tabs with the same [id] exists,
   * otherwise existing tab with the [id] will be closed and new tab opened
   */
  val reuseTabOnRequest: Boolean
}