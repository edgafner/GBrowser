package com.github.gib.ui.toolwindow.base

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowEx

fun ToolWindow.dontHideOnEmptyContent() {
  setToHideOnEmptyContent(false)
  (this as? ToolWindowEx)?.emptyText?.text = ""
}