package com.github.gbrowser.ui.toolwindow.create

import com.github.gbrowser.ui.toolwindow.GBrowserViewTabsFactory
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import javax.swing.Icon


internal class GBrowserCreateComponentHolder(disposable: Disposable, callBack: (Icon) -> Unit) {


  private val uiDisposable = Disposer.newDisposable().also {
    Disposer.register(disposable, it)
  }

  val component by lazy {
    GBrowserViewTabsFactory(uiDisposable, callBack).create()
  }


}
