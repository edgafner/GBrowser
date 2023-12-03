package com.github.gbrowser.ui.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import com.intellij.ui.tabs.impl.SingleHeightTabs
import javax.swing.JComponent

internal class GBrowserViewTabsFactory(private val project: Project, private val disposable: Disposable) {
  private val uiDisposable = Disposer.newDisposable().also {
    Disposer.register(disposable, it)
  }

  fun create(infoComponent: JComponent): JBTabs {

    val infoTabInfo = TabInfo(infoComponent)

    return object : SingleHeightTabs(project, uiDisposable) {
      override fun adjust(tabInfo: TabInfo) = Unit
    }.apply {
      addTab(infoTabInfo)
    }.also {
      val listener = object : TabsListener {
        override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {

        }
      }
      it.addListener(listener)
      listener.selectionChanged(null, it.selectedInfo)
    }
  }

}