package com.github.gib.ui.toolwindow.model

import com.github.gib.services.GivServiceSettings
import com.github.gib.ui.toolwindow.base.GBrowserToolwindowViewModel
import com.intellij.collaboration.async.mapScoped
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.github.gib.uitl.childScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class GBrowserToolWindowViewModel internal constructor(private val project: Project, parentCs: CoroutineScope) :
  GBrowserToolwindowViewModel<GBrowserToolWindowProjectViewModel> {

  private val settings: GivServiceSettings
    get() = GivServiceSettings.instance()


  //TODO: switch to Default dispatcher
  private val cs = parentCs.childScope(Dispatchers.Main)

  val isAvailable: StateFlow<Boolean> = flowOf(true).stateIn(cs, SharingStarted.Lazily, true)

  private val _activationRequests = MutableSharedFlow<Unit>(1)
  internal val activationRequests: Flow<Unit> = _activationRequests.asSharedFlow()

  @Suppress("UnstableApiUsage")
  override val projectVm: StateFlow<GBrowserToolWindowProjectViewModel?> by lazy {
    MutableStateFlow(null).asStateFlow().mapScoped {
      GBrowserToolWindowProjectViewModel(project, this, settings)
    }.stateIn(cs, SharingStarted.Lazily, null)

  }


  fun activate() {
    _activationRequests.tryEmit(Unit)
  }

  fun activateAndAwaitProject(action: GBrowserToolWindowProjectViewModel.() -> Unit) {
    cs.launch {
      _activationRequests.emit(Unit)
      projectVm.filterNotNull().first().action()
    }
  }


}