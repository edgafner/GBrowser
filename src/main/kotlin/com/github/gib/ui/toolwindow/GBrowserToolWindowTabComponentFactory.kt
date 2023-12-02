package com.github.gib.ui.toolwindow

import com.github.gib.GivMainPanel
import com.github.gib.services.GivServiceSettings
import com.github.gib.ui.toolwindow.base.GBrowserTabsComponentFactory
import com.github.gib.ui.toolwindow.create.GBrowserCreateComponentHolder
import com.github.gib.ui.toolwindow.model.GBrowserToolWindowProjectViewModel
import com.github.gib.ui.toolwindow.model.GBrowserToolWindowTabViewModel
import com.intellij.collaboration.async.launchNow
import com.intellij.collaboration.async.nestedDisposable
import com.intellij.collaboration.ui.CollaborationToolsUIUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import javax.swing.Icon
import javax.swing.JComponent

internal class GBrowserToolWindowTabComponentFactory(private val project: Project) : GBrowserTabsComponentFactory<GBrowserToolWindowTabViewModel, GBrowserToolWindowProjectViewModel> {


  override fun createGBrowserComponent(cs: CoroutineScope, projectVm: GBrowserToolWindowProjectViewModel): JComponent {
    return GivMainPanel(GivServiceSettings.instance().getLastSaveHomePage(), {}, cs)
  }

  override fun createTabComponent(cs: CoroutineScope,
                                  projectVm: GBrowserToolWindowProjectViewModel,
                                  tabVm: GBrowserToolWindowTabViewModel,
                                  callBack: (Icon) -> Unit,
                                  contentCs: CoroutineScope): JComponent {
    return when (tabVm) {
      is GBrowserToolWindowTabViewModel.NewBrowserTab -> cs.createNewGBrowserComponent(projectVm, tabVm, callBack, contentCs)
    }
  }


  @Suppress("UnstableApiUsage")
  private fun CoroutineScope.createNewGBrowserComponent(
    projectVm: GBrowserToolWindowProjectViewModel,
    tabVm: GBrowserToolWindowTabViewModel.NewBrowserTab,
    callBack: (Icon) -> Unit,
    contentCs: CoroutineScope,
  ): JComponent {
    val settings = GivServiceSettings.instance()
    return GBrowserCreateComponentHolder(ActionManager.getInstance(), project, projectVm, settings,
                                         nestedDisposable(), callBack, contentCs).component.also { comp ->
      launchNow {
        tabVm.focusRequests.collect {
          yield()
          CollaborationToolsUIUtil.focusPanel(comp)
        }
      }
    }
  }
}