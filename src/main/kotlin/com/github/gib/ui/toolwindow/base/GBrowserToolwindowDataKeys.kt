package com.github.gib.ui.toolwindow.base

import com.intellij.openapi.actionSystem.DataKey

object GBrowserToolwindowDataKeys {
  @JvmStatic
  val GBROWSER_TOOLWINDOW_PROJECT_VM =
    DataKey.create<GBrowserToolwindowProjectViewModel<*, *>>("com.github.gib.ui.toolwindow.base.project.vm")

  @JvmStatic
  val GBROWSER_TOOLWINDOW_VM = DataKey.create<GBrowserToolwindowViewModel<*>>("com.github.gib.ui.toolwindow.base.toolwindow.vm")
}