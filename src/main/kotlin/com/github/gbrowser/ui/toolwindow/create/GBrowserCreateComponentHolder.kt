package com.github.gbrowser.ui.toolwindow.create

import com.github.gbrowser.GivMainPanel
import com.github.gbrowser.services.GivServiceSettings
import com.github.gbrowser.ui.toolwindow.GBrowserViewTabsFactory
import com.github.gbrowser.ui.toolwindow.model.GBrowserToolWindowProjectViewModel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CoroutineScope
import javax.swing.Icon


internal class GBrowserCreateComponentHolder(private val actionManager: ActionManager,
                                             private val project: Project,
                                             private val projectVm: GBrowserToolWindowProjectViewModel,
                                             private val settings: GivServiceSettings,
                                             disposable: Disposable,
                                             callBack: (Icon) -> Unit,
                                             contentCs: CoroutineScope) {


  private val uiDisposable = Disposer.newDisposable().also {
    Disposer.register(disposable, it)
  }

  val component by lazy {
    val infoComponent = GivMainPanel(settings.getLastSaveHomePage(), callBack,contentCs)
    GBrowserViewTabsFactory(project, uiDisposable).create(infoComponent).component
  }


}
