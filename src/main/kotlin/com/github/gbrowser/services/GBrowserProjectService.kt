package com.github.gbrowser.services

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserTab
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.EventDispatcher
import kotlinx.serialization.Serializable
import java.util.*

@Service(Service.Level.PROJECT)
@State(name = "GBrowserProjectService", storages = [Storage(value = "gbrowser_project.xml")], category = SettingsCategory.TOOLS)
class GBrowserProjectService : SerializablePersistentStateComponent<GBrowserProjectService.ProjectSettingsState>(ProjectSettingsState()) {

  private val listeners = EventDispatcher.create(Listener::class.java)

  @Serializable
  data class ProjectSettingsState(var tabs: MutableSet<GBrowserTab> = mutableSetOf())

  var tabs: MutableSet<GBrowserTab>
    get() = state.tabs
    set(value) {
      updateStateAndEmit {
        it.copy(tabs = value)
      }
    }

  fun addTab(tab: GBrowserTab) {
    updateStateAndEmit {
      it.copy(tabs = it.tabs.apply { add(tab) })
    }
  }

  fun addTabs(tabs: List<GBrowserTab>) {
    updateStateAndEmit {
      it.copy(tabs = it.tabs.apply { addAll(tabs) })
    }
  }

  @Suppress("unused")
  fun removeTab(tab: GBrowserTab) {
    updateStateAndEmit {
      it.copy(tabs = it.tabs.apply { remove(tab) })
    }
  }

  private inline fun updateStateAndEmit(updateFunction: (currentState: ProjectSettingsState) -> ProjectSettingsState) {
    val state = super.updateState(updateFunction)
    listeners.multicaster.onSettingsChange(state)
  }

  @Suppress("unused")
  fun addListener(listener: Listener) = listeners.addListener(listener)

  fun interface Listener : EventListener {
    fun onSettingsChange(settings: ProjectSettingsState)
  }


  companion object {
    @Suppress("unused")
    private val LOG = logger<GBrowserProjectService>()

  }

}



