package com.github.gbrowser.settings

import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.settings.dao.GBrowserHistoryDelete
import com.github.gbrowser.settings.dao.GBrowserHistory
import com.github.gbrowser.settings.request_header.GBrowserRequestHeader
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.EventDispatcher
import kotlinx.serialization.Serializable
import java.util.*


@State(name = "GBrowserPersistSetting", storages = [Storage(value = "gbrowser.xml")], category = SettingsCategory.TOOLS)
class GBrowserSetting : SerializablePersistentStateComponent<GBrowserSetting.SettingsState>(SettingsState()) {

  private val listeners = EventDispatcher.create(Listener::class.java)

  @Serializable
  data class SettingsState(var gbrowserDefaultHomePage: String = "https://dorkag.com/dorkag",
                           var isToolWindowTitleVisible: Boolean = true,
                           var showBookMarksInToolbar: Boolean = true,
                           var isHistoryEnabled: Boolean = true,
                           var isSuggestionSearchEnabled: Boolean = false,
                           var isSuggestionSearchHighlighted: Boolean = true,
                           var isHostHighlight: Boolean = true,
                           var isUnSelectedTabIconTransparent: Boolean = false,
                           var isFavIconEnabled: Boolean = true,
                           var isTabIconVisible: Boolean = true,
                           var isDebugEnabled: Boolean = false,
                           var debugPort: Int = GBrowserUtil.getJCEFDebugPort(),
                           var isProtocolHidden: Boolean = true,
                           var isDnDEnabled: Boolean = false,
                           var historyDeleteOptions: MutableList<GBrowserHistoryDelete> = mutableListOf(
                             GBrowserHistoryDelete(-1, "Delete never"), GBrowserHistoryDelete(0, "Delete on close IDE"),
                             GBrowserHistoryDelete(24, "Delete after 1 day"), GBrowserHistoryDelete(168, "Delete after 7 days"),
                             GBrowserHistoryDelete(336, "Delete after 14 days"), GBrowserHistoryDelete(720, "Delete after 30 days"),
                             GBrowserHistoryDelete(1440, "Delete after 60 days")),
                           var historyDeleteOption: GBrowserHistoryDelete = GBrowserHistoryDelete(24, "Delete after 1 day"),
                           var requestHeaders: MutableSet<GBrowserRequestHeader> = mutableSetOf(GBrowserRequestHeader(
                             "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 /CefSharp Browser 90.0",
                             "User-Agent", true)),
                           var history: MutableSet<GBrowserHistory> = mutableSetOf(),
                           var bookmarks: MutableSet<GBrowserBookmark> = mutableSetOf())


  var isProtocolHidden: Boolean
    get() = state.isProtocolHidden
    set(value) {
      updateStateAndEmit {
        it.copy(isProtocolHidden = value)
      }
    }

  var history: MutableSet<GBrowserHistory>
    get() = state.history
    set(value) {
      updateStateAndEmit {
        it.copy(history = value)
      }
    }

  var requestHeaders: MutableSet<GBrowserRequestHeader>
    get() = state.requestHeaders
    set(value) {
      updateStateAndEmit {
        it.copy(requestHeaders = value)
      }
    }

  var isToolWindowTitleVisible: Boolean
    get() = state.isToolWindowTitleVisible
    set(value) {
      updateStateAndEmit {
        it.copy(isToolWindowTitleVisible = value)
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

  @Suppress("unused")
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

  var isDnDEnabled: Boolean
    get() = state.isDnDEnabled
    set(value) {
      updateStateAndEmit {
        it.copy(isDnDEnabled = value)
      }
    }

  var defaultUrl: String
    get() = state.gbrowserDefaultHomePage
    set(value) {
      updateStateAndEmit {
        it.copy(gbrowserDefaultHomePage = value)
      }
    }

  var historyDeleteOption: GBrowserHistoryDelete
    get() = state.historyDeleteOption
    set(value) {
      updateStateAndEmit {
        it.copy(historyDeleteOption = value)
      }
    }

  var historyDeleteOptions: MutableList<GBrowserHistoryDelete>
    get() = state.historyDeleteOptions
    set(value) {
      updateStateAndEmit {
        it.copy(historyDeleteOptions = value)
      }
    }


  fun addHistory(history: GBrowserHistory) {
    updateStateAndEmit {
      val existingHistory = it.history.find { h -> h.url == history.url }
      if (existingHistory != null) {
        if (existingHistory.name.isBlank() && history.name.isNotBlank()) { // Remove the existing history and add the new one
          it.history.remove(existingHistory)
          it.history.add(history)
        }
      } else { // Add new history if no existing history with the same URL
        it.history.add(history)
      }
      it  // Return the modified state
    }
  }


  @Suppress("unused")
  fun addHistory(histories: List<GBrowserHistory>) {
    histories.forEach { history ->
      addHistory(history)
    }
  }


  fun removeHistory() {
    updateStateAndEmit {
      it.copy(history = it.history.apply { clear() })
    }
  }

  @Suppress("unused")
  fun removeHistory(history: GBrowserHistory) {
    updateStateAndEmit {
      it.copy(history = it.history.apply { remove(history) })
    }
  }

  fun removeHistory(fromDate: Date) {
    updateStateAndEmit {
      it.copy(history = it.history.apply { removeAll { h -> h.createdAt.before(fromDate) } })
    }
  }


  var bookmarks: MutableSet<GBrowserBookmark>
    get() = state.bookmarks
    set(value) {
      updateStateAndEmit {
        it.copy(bookmarks = value)
      }
    }

  fun addBookmarks(bookmarks: List<GBrowserBookmark>) {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { addAll(bookmarks.toSet()) })
    }
  }

  fun addBookmarks(bookmark: GBrowserBookmark) {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { add(bookmark) })
    }
  }

  fun removeBookmarks(bookmark: GBrowserBookmark) {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { remove(bookmark) })
    }
  }

  @Suppress("unused")
  fun removeBookmarks(bookmarks: List<GBrowserBookmark>) {
    updateStateAndEmit {
      it.copy(bookmarks = it.bookmarks.apply { removeAll(bookmarks.toSet()) })
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

  @Suppress("unused")
  fun addListener(listener: Listener) = listeners.addListener(listener)

  fun interface Listener : EventListener {
    fun onSettingsChange(settings: SettingsState)
  }


  companion object {
    @Suppress("unused")
    private val LOG = logger<GBrowserSetting>()

    @JvmStatic
    fun instance(): GBrowserSetting {
      return ApplicationManager.getApplication().getService(GBrowserSetting::class.java)
    }
  }

}