package com.github.gbrowser.ui.toolwindow.base

import org.jetbrains.annotations.Nls
import javax.swing.Icon

/**
 * View model of the toolwindow tab content
 */

interface GBrowserTabViewModel {

  /**
   * Toolwindow tab title for debug
   */
  val displayName: @Nls String

  val icon: @Nls Icon

}