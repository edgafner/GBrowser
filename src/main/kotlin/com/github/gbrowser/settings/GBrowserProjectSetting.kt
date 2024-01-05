package com.github.gbrowser.settings


import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserTab
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.EventDispatcher
import kotlinx.serialization.Serializable
import java.util.*


@Service(Service.Level.PROJECT)
@State(name = "GBrowserProjectSetting", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)], reportStatistic = false)
class GBrowserProjectSetting : SerializablePersistentStateComponent<GBrowserProjectSetting.ProjectSettingsState>(ProjectSettingsState()) {

  private val listeners = EventDispatcher.create(Listener::class.java)

  @Serializable
  data class ProjectSettingsState(var tabs: MutableList<GBrowserTab> = mutableListOf())

  @Suppress("unused")
  var tabs: MutableList<GBrowserTab>
    get() = state.tabs
    set(value) {
      updateStateAndEmit {
        it.copy(tabs = value)
      }
    }

  @Suppress("unused")
  fun addTab(tab: GBrowserTab) {
    updateStateAndEmit {
      it.copy(tabs = it.tabs.apply { add(tab) })
    }
  }

  @Suppress("unused")
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

  @Suppress("unused")
  fun removeTabs(tabs: List<GBrowserTab>) {
    updateStateAndEmit {
      it.copy(tabs = it.tabs.apply { removeAll(tabs) })
    }
  }

  fun removeTabs(date: Date) {
    updateStateAndEmit {
      it.copy(tabs = it.tabs.apply { removeAll { tb -> tb.createdAt == date } })
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
    private val LOG = logger<GBrowserProjectSetting>()

  }

}



