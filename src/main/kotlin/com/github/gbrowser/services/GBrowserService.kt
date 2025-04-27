package com.github.gbrowser.services

import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.settings.dao.GBrowserHistory
import com.github.gbrowser.settings.request_header.GBrowserRequestHeader
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.EventDispatcher
import kotlinx.serialization.Serializable
import java.util.*

@State(
  name = "GBrowserService", storages = [Storage(value = "gbrowsers.xml"), Storage(value = "gbrowser_service.xml", deprecated = true), Storage(
    value = "gbrowser.xml", deprecated = true
  )], category = SettingsCategory.TOOLS
)
@Service(Service.Level.PROJECT)
class GBrowserService : SerializablePersistentStateComponent<GBrowserService.SettingsState>(SettingsState()) {

  private val listeners = EventDispatcher.create(Listener::class.java)

  @Serializable
  data class SettingsState(var defaultHomePage: String = "https://plugins.jetbrains.com/plugin/14458-gbrowser",
                           var hideIdLabel: Boolean = true,
                           var isReloadTabsOnStartup: Boolean = false,
                           var showBookMarksInToolbar: Boolean = true,
                           var isHistoryEnabled: Boolean = true,
                           var isSuggestionSearchEnabled: Boolean = false,
                           var isSuggestionSearchHighlighted: Boolean = true,
                           var isHostHighlight: Boolean = true,
                           var isUnSelectedTabIconTransparent: Boolean = false,
                           var isFavIconEnabled: Boolean = true,
                           var isTabIconVisible: Boolean = true,
                           var navigateInNewTab: Boolean = true,
                           var isDebugEnabled: Boolean = false,
                           var debugPort: Int = if (GBrowserUtil.getJCEFDebugPort() == -1) 9222 else GBrowserUtil.getJCEFDebugPort(),
                           var isProtocolHidden: Boolean = true,
                           var isDragAndDropEnabled: Boolean = true,
                           var historyItemsToKeep: Int = 20,
                           var requestHeaders: MutableSet<GBrowserRequestHeader> = mutableSetOf(
                             GBrowserRequestHeader(
                               "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 /CefSharp Browser 90.0",
                               "User-Agent",
                               true
                             )
                           ),
                           var history: LinkedHashSet<GBrowserHistory> = linkedSetOf(),
                           var bookmarks: MutableSet<GBrowserBookmark> = mutableSetOf())


  var isProtocolHidden: Boolean
    get() = state.isProtocolHidden
    set(value) {
      updateStateAndEmit {
        it.copy(isProtocolHidden = value)
      }
    }


  var requestHeaders: MutableSet<GBrowserRequestHeader>
    get() = state.requestHeaders
    set(value) {
      updateStateAndEmit {
        it.copy(requestHeaders = value)
      }
    }

  var hideIdLabel: Boolean
    get() = state.hideIdLabel
    set(value) {
      updateStateAndEmit {
        it.copy(hideIdLabel = value)
      }
    }

  var showBookMarksInToolbar: Boolean
    get() = state.showBookMarksInToolbar
    set(value) {
      updateStateAndEmit {
        it.copy(showBookMarksInToolbar = value)
      }
    }

  var isHistoryEnabled: Boolean
    get() = state.isHistoryEnabled
    set(value) {
      updateStateAndEmit {
        it.copy(isHistoryEnabled = value)
      }
    }

  var isSuggestionSearchEnabled: Boolean
    get() = state.isSuggestionSearchEnabled
    set(value) {
      updateStateAndEmit {
        it.copy(isSuggestionSearchEnabled = value)
      }
    }

  var isSuggestionSearchHighlighted: Boolean
    get() = state.isSuggestionSearchHighlighted
    set(value) {
      updateStateAndEmit {
        it.copy(isSuggestionSearchHighlighted = value)
      }
    }

  var isHostHighlight: Boolean
    get() = state.isHostHighlight
    set(value) {
      updateStateAndEmit {
        it.copy(isHostHighlight = value)
      }
    }

  var isUnSelectedTabIconTransparent: Boolean
    get() = state.isUnSelectedTabIconTransparent
    set(value) {
      updateStateAndEmit {
        it.copy(isUnSelectedTabIconTransparent = value)
      }
    }

  var isFavIconEnabled: Boolean
    get() = state.isFavIconEnabled
    set(value) {
      updateStateAndEmit {
        it.copy(isFavIconEnabled = value)
      }
    }

  var navigateInNewTab: Boolean
    get() = state.navigateInNewTab
    set(value) {
      updateStateAndEmit {
        it.copy(navigateInNewTab = value)
      }
    }

  var isTabIconVisible: Boolean
    get() = state.isTabIconVisible
    set(value) {
      updateStateAndEmit {
        it.copy(isTabIconVisible = value)
      }
    }

  var isDebugEnabled: Boolean
    get() = state.isDebugEnabled
    set(value) {
      updateStateAndEmit {
        it.copy(isDebugEnabled = value)
      }
    }

  var debugPort: Int
    get() = state.debugPort
    set(value) {
      GBrowserUtil.setJCEFDebugPort(value)
      updateStateAndEmit {
        it.copy(debugPort = value)
      }
    }

  var isDragAndDropEnabled: Boolean
    get() = state.isDragAndDropEnabled
    set(value) {
      updateStateAndEmit {
        it.copy(isDragAndDropEnabled = value)
      }
    }

  var defaultUrl: String
    get() = state.defaultHomePage
    set(value) {
      updateStateAndEmit {
        it.copy(defaultHomePage = value)
      }
    }

  var historyItemsToKeep: Int
    get() = state.historyItemsToKeep
    set(value) {
      updateStateAndEmit {
        it.copy(historyItemsToKeep = value)
      }
    }

  var history: LinkedHashSet<GBrowserHistory>
    get() = state.history
    set(value) {
      updateStateAndEmit {
        it.copy(history = value)
      }
    }

  fun addHistory(historyItem: GBrowserHistory) {
    updateStateAndEmit { // Check if the history item exists and update it if necessary
      val existingHistory = it.history.find { h -> h.url == historyItem.url }
      if (existingHistory != null) {
        if (existingHistory.name.isBlank() && historyItem.name.isNotBlank()) {
          it.history.remove(existingHistory)
          it.history.add(historyItem)
        }
      } else { // Add new history item, removing the oldest one if the size limit is reached
        if (it.history.size >= state.historyItemsToKeep) {
          it.history.remove(it.history.first())
        }
        it.history.add(historyItem)
      }
      it
    }
  }


  fun removeHistory() {
    updateStateAndEmit {
      it.copy(history = it.history.apply { clear() })
    }
  }

  var reloadTabOnStartup: Boolean
    get() = state.isReloadTabsOnStartup
    set(value) {
      updateStateAndEmit {
        it.copy(isReloadTabsOnStartup = value)
      }
    }


  var bookmarks: MutableSet<GBrowserBookmark>
    get() = state.bookmarks
    set(value) {
      updateStateAndEmit {
        it.copy(bookmarks = value)
      }
    }

  fun addBookmarks(bookmarks: MutableSet<GBrowserBookmark>) {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { addAll(bookmarks.toSet()) })
    }
  }

  fun addBookmarks(bookmark: GBrowserBookmark) {
    updateStateAndEmit {
      if (!bookmarks.contains(bookmark)) {
        it.copy(bookmarks = it.bookmarks.apply { add(bookmark) })
      } else {
        it
      }
    }
  }

  fun removeBookmark(bookmark: GBrowserBookmark) {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { remove(bookmark) })
    }
  }

  @Suppress("unused")
  fun removeBookmarks() {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { clear() })
    }
  }

  fun existBookmark(url: String): Boolean {
    return state.bookmarks.any { fav -> fav.url == url }
  }


  @Suppress("unused")
  fun addRequestHeader(requestHeader: GBrowserRequestHeader) {
    updateStateAndEmit {
      it.copy(requestHeaders = it.requestHeaders.apply { add(requestHeader) })
    }
  }

  fun addRequestHeader(requestHeaders: List<GBrowserRequestHeader>) {
    updateStateAndEmit {
      it.copy(requestHeaders = it.requestHeaders.apply { addAll(requestHeaders.toSet()) })
    }
  }

  @Suppress("unused")
  fun removeRequestHeader(requestHeader: GBrowserRequestHeader) {
    updateStateAndEmit {
      it.copy(requestHeaders = it.requestHeaders.apply { remove(requestHeader) })
    }
  }

  @Suppress("unused")
  fun removeRequestHeader(requestHeaders: List<GBrowserRequestHeader>) {
    updateStateAndEmit {
      it.copy(requestHeaders = it.requestHeaders.apply { removeAll(requestHeaders.toSet()) })
    }
  }


  private inline fun updateStateAndEmit(updateFunction: (currentState: SettingsState) -> SettingsState) {
    val state = super.updateState(updateFunction)
    listeners.multicaster.onSettingsChange(state)
  }

  fun addListener(listener: Listener) = listeners.addListener(listener)

  fun interface Listener : EventListener {
    fun onSettingsChange(settings: SettingsState)
  }


  companion object {
    @Suppress("unused")
    private val LOG = logger<GBrowserService>()

  }

}