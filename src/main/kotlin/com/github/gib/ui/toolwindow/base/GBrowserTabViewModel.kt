package com.github.gib.ui.toolwindow.base

import org.jetbrains.annotations.Nls
import javax.swing.Icon

/**
 * View model of the review toolwindow tab content
 */
//TODO: move name and description to component factory and potentially remove this class
interface GBrowserTabViewModel {

  /**
   * Toolwindow tab title for debug
   */
  val displayName: @Nls String

  val icon: @Nls Icon

}