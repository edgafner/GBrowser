package com.github.gbrowser.ui.toolwindow

import com.github.gbrowser.GBrowserMainPanel
import com.github.gbrowser.services.GivServiceSettings
import com.github.gbrowser.ui.toolwindow.base.GBrowserTabsComponentFactory
import com.github.gbrowser.ui.toolwindow.create.GBrowserCreateComponentHolder
import com.github.gbrowser.ui.toolwindow.model.GBrowserToolWindowProjectViewModel
import com.github.gbrowser.ui.toolwindow.model.GBrowserToolWindowTabViewModel
import com.intellij.collaboration.async.launchNow
import com.intellij.collaboration.async.nestedDisposable
import com.intellij.openapi.wm.IdeFocusManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import javax.swing.Icon
import javax.swing.JComponent

internal class GBrowserToolWindowTabComponentFactory : GBrowserTabsComponentFactory<GBrowserToolWindowTabViewModel, GBrowserToolWindowProjectViewModel> {


  override fun createGBrowserComponent(cs: CoroutineScope, projectVm: GBrowserToolWindowProjectViewModel): JComponent {

    return GBrowserMainPanel(GivServiceSettings.instance().getLastSaveHomePage(), {}, cs)
  }

  override fun createTabComponent(cs: CoroutineScope,
                                  projectVm: GBrowserToolWindowProjectViewModel,
                                  tabVm: GBrowserToolWindowTabViewModel,
                                  callBack: (Icon) -> Unit): JComponent {
    return when (tabVm) {
      is GBrowserToolWindowTabViewModel.NewBrowserTab -> cs.createNewGBrowserComponent(tabVm, callBack)
    }
  }


  @Suppress("UnstableApiUsage")
  private fun CoroutineScope.createNewGBrowserComponent(tabVm: GBrowserToolWindowTabViewModel.NewBrowserTab,
                                                        callBack: (Icon) -> Unit): JComponent {

    return GBrowserCreateComponentHolder(nestedDisposable(), callBack).component.also { comp ->
      launchNow {
        tabVm.focusRequests.collect {
          yield()
          val focusManager = IdeFocusManager.findInstanceByComponent(comp)
          val toFocus = focusManager.getFocusTargetFor(comp) ?: return@collect
          focusManager.doWhenFocusSettlesDown { focusManager.requestFocus(toFocus, true) }
        }
      }
    }
  }
}