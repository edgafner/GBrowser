package com.github.gbrowser.ui.toolwindow

import com.github.gbrowser.GBrowserMainPanel
import com.github.gbrowser.services.GBrowserSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.swing.Icon
import javax.swing.JComponent

internal class GBrowserViewTabsFactory(private val disposable: Disposable, private val callBack: (Icon) -> Unit) {
  private val uiDisposable = Disposer.newDisposable().also {
    Disposer.register(disposable, it)
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.EDT + ModalityState.defaultModalityState().asContextElement()).also {
    Disposer.register(uiDisposable) { it.cancel() }
  }

  val settings = GBrowserSettings.instance()

  fun create(): JComponent {

    return GBrowserMainPanel(settings.getHomePage(), callBack, scope)
  }

}