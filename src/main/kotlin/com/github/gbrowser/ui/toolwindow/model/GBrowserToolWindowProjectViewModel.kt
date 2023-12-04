package com.github.gbrowser.ui.toolwindow.model

import com.github.gbrowser.services.GBrowserSettings
import com.github.gbrowser.ui.toolwindow.GBrowserToolWindowTab
import com.github.gbrowser.ui.toolwindow.base.GBrowserToolwindowProjectViewModel
import com.github.gbrowser.ui.toolwindow.base.GBrowserToolwindowTabs
import com.intellij.openapi.project.Project
import com.intellij.util.childScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


@Suppress("UnstableApiUsage")
class GBrowserToolWindowProjectViewModel internal constructor(project: Project,
                                                              parentCs: CoroutineScope,
                                                              private val settings: GBrowserSettings) : GBrowserToolwindowProjectViewModel<GBrowserToolWindowTab, GBrowserToolWindowTabViewModel> {
  private val cs = parentCs.childScope()

  override val browserVm: GBrowserViewModel = GBrowserViewModelImpl()


  override val projectName: String = project.name


  private val _tabs = MutableStateFlow<GBrowserToolwindowTabs<GBrowserToolWindowTab, GBrowserToolWindowTabViewModel>>(
    GBrowserToolwindowTabs(emptyMap(), null))
  override val tabs: StateFlow<GBrowserToolwindowTabs<GBrowserToolWindowTab, GBrowserToolWindowTabViewModel>> = _tabs.asStateFlow()

  private val tabsGuard = Mutex()

  private inline fun <reified T, reified VM> showTab(tab: T,
                                                     crossinline vmProducer: (T) -> VM,
                                                     crossinline processVM: VM.() -> Unit = {}) where T : GBrowserToolWindowTab, VM : GBrowserToolWindowTabViewModel {
    cs.launch {
      tabsGuard.withLock {
        val current = _tabs.value
        val currentVm = current.tabs[tab]
        if (currentVm == null || currentVm !is VM || !tab.reuseTabOnRequest) {
          currentVm?.destroy()
          val tabVm = vmProducer(tab).apply(processVM)
          _tabs.value = current.copy(current.tabs + (tab to tabVm), tab)
        }
        else {
          currentVm.apply(processVM)
          _tabs.value = current.copy(selectedTab = tab)
        }
      }
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun createVm(tab: GBrowserToolWindowTab.NewBrowserTab): GBrowserToolWindowTabViewModel.NewBrowserTab = GBrowserToolWindowTabViewModel.NewBrowserTab(
    settings.getHomePage())

  override fun selectTab(tab: GBrowserToolWindowTab?) {
    cs.launch {
      tabsGuard.withLock {
        _tabs.update {
          it.copy(selectedTab = tab)
        }
      }
    }
  }

  override fun closeTab(tab: GBrowserToolWindowTab) {
    cs.launch {
      tabsGuard.withLock {
        val current = _tabs.value
        val currentVm = current.tabs[tab]
        if (currentVm != null) {
          currentVm.destroy()
          _tabs.value = current.copy(current.tabs - tab, null)
        }
      }
    }
  }

  fun createNewGBrowserTab(id: String, requestFocus: Boolean = true) {
    showTab(GBrowserToolWindowTab.NewBrowserTab(id), ::createVm) {
      if (requestFocus) {
        requestFocus()
      }
    }
  }


}