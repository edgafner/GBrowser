package com.github.gbrowser.ui.toolwindow.model

import com.github.gbrowser.services.GBrowserSettings
import com.github.gbrowser.ui.toolwindow.base.GBrowserToolwindowViewModel
import com.github.gbrowser.util.mapScoped
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.childScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Suppress("UnstableApiUsage")
@Service(Service.Level.PROJECT)
internal class GBrowserToolWindowViewModel(private val project: Project,
                                           parentCs: CoroutineScope) : GBrowserToolwindowViewModel<GBrowserToolWindowProjectViewModel> {

  private val settings: GBrowserSettings = GBrowserSettings.instance()
  private val cs = parentCs.childScope(Dispatchers.Main)

  val isAvailable: StateFlow<Boolean> = flowOf(true).stateIn(cs, SharingStarted.Lazily, true)

  private val _activationRequests = MutableSharedFlow<Unit>(1)
  internal val activationRequests: Flow<Unit> = _activationRequests.asSharedFlow()

  override val projectVm: StateFlow<GBrowserToolWindowProjectViewModel?> by lazy {
    MutableStateFlow(null).asStateFlow().mapScoped {
      GBrowserToolWindowProjectViewModel(project, this, settings)
    }.stateIn(cs, SharingStarted.Lazily, GBrowserToolWindowProjectViewModel(project, cs, settings))

  }


  fun activateAndAwaitProject(action: GBrowserToolWindowProjectViewModel.() -> Unit) {
    cs.launch {
      _activationRequests.emit(Unit)
      projectVm.filterNotNull().first().action()
    }
  }


}


