package com.github.gbrowser.ui.toolwindow.base

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents view model for review toolwindow that holds selected project VM [projectVm] (for GitHub it is a repository).
 *
 * Clients can provide more specific methods in implementation and acquire the view model using [GBrowserToolwindowDataKeys.GBROWSER_TOOLWINDOW_VM]
 */
interface GBrowserToolwindowViewModel<PVM : GBrowserToolwindowProjectViewModel<*, *>> {
  val projectVm: StateFlow<PVM?>
}