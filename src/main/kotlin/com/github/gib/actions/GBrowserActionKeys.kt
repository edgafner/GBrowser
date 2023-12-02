package com.github.gib.actions

import com.github.gib.ui.toolwindow.model.GBrowserToolWindowProjectViewModel
import com.intellij.openapi.actionSystem.DataKey

object GBrowserActionKeys {


  @JvmStatic
  val GBROWSER_PROJECT_VM = DataKey.create<GBrowserToolWindowProjectViewModel>("com.github.gib.project.vm")


}