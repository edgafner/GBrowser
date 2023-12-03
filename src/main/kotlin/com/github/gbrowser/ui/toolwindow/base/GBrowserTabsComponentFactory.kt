package com.github.gbrowser.ui.toolwindow.base

import kotlinx.coroutines.CoroutineScope
import javax.swing.Icon
import javax.swing.JComponent

/**
 * Provides UI components for gbrowser toolwindow tabs and toolwindow empty state.
 */
interface GBrowserTabsComponentFactory<TVM : GBrowserTabViewModel, PVM : GBrowserToolwindowProjectViewModel<*, TVM>> {
  /**
   * Provide a gbrowser list component for given [projectVm]
   *
   * @param cs scope that closes when context is changed
   */
  fun createGBrowserComponent(cs: CoroutineScope, projectVm: PVM): JComponent

  /**
   * Provides a component for given [tabVm] and [projectVm]
   *
   * @param cs scope that closes when tab is closed or context changed
   */
  fun createTabComponent(cs: CoroutineScope, projectVm: PVM, tabVm: TVM, callBack: (Icon) -> Unit): JComponent


}