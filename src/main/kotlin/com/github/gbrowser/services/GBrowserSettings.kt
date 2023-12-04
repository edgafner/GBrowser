package com.github.gbrowser.services

import com.github.gbrowser.settings.GBrowserBookmarks
import com.github.gbrowser.settings.GBrowserHeadersOverwrite
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger

@State(name = "GivServiceSettings", storages = [Storage(value = "gbrowser.xml")], category = SettingsCategory.TOOLS)
internal class GBrowserSettings : PersistentStateComponent<GBrowserSettings.State> {

  private val lock = Any()
  private var myState = State()

  companion object {
    private val LOG = logger<GBrowserSettings>()

    @JvmStatic
    fun instance(): GBrowserSettings {
      return ApplicationManager.getApplication().getService(GBrowserSettings::class.java)
    }
  }

  fun saveHomePage(homePage: String) = synchronized(lock) {
    myState.homePage = homePage
  }

  fun getHomePage(): String {
    return myState.homePage
  }

  fun getBookmarks(): MutableList<GBrowserBookmarks> {
    return myState.bookmarks.distinctBy { it.webUrl }.toMutableList()
  }

  fun getQuickAccessBookmarks(): MutableList<GBrowserBookmarks> {
    return myState.quickAccessBookmarks.distinctBy { it.webUrl }.toMutableList()
  }

  fun getHeadersOverwrite(): List<GBrowserHeadersOverwrite> {
    return myState.myGBrowserHeadersOverwrite
  }

  fun addToBookmarks(gBrowserBookmarks: List<GBrowserBookmarks>) = synchronized(lock) {
    myState.bookmarks.removeAll { true }
    try {
      myState.bookmarks.addAll(gBrowserBookmarks.distinctBy { it.webUrl })
    }
    catch (e: Exception) {
      LOG.warn("Error adding bookmarks", e)
    }
  }

  fun addToBookmarks(gBrowserBookmarks: GBrowserBookmarks) = synchronized(lock) {
    try {
      if (myState.bookmarks.none { it.webUrl == gBrowserBookmarks.webUrl }) {
        myState.bookmarks.add(gBrowserBookmarks)
      }
    }
    catch (e: Exception) {
      LOG.warn("Error adding bookmarks", e)
    }
  }

  fun addToQuickAccessBookmarks(gBrowserBookmarks: List<GBrowserBookmarks>) = synchronized(lock) {
    myState.quickAccessBookmarks.removeAll { true }
    try {
      myState.quickAccessBookmarks.addAll(gBrowserBookmarks.distinctBy { it.webUrl })
    }
    catch (e: Exception) {
      LOG.warn("Error adding other bookmarks", e)
    }
  }

  fun addToQuickAccessBookmarks(gBrowserBookmarks: GBrowserBookmarks) = synchronized(lock) {
    try {
      if (myState.quickAccessBookmarks.none { it.webUrl == gBrowserBookmarks.webUrl }) {
        myState.quickAccessBookmarks.add(gBrowserBookmarks)
      }
    }
    catch (e: Exception) {
      LOG.warn("Error adding bookmarks", e)
    }
  }


  fun addToHeadersOverwrite(gBrowserHeadersOverwrites: List<GBrowserHeadersOverwrite>) = synchronized(lock) {
    myState.myGBrowserHeadersOverwrite = gBrowserHeadersOverwrites
  }

  class State {
    var homePage: String = "https://dorkag.com/dorkag"
    var bookmarks = mutableListOf<GBrowserBookmarks>()
    var quickAccessBookmarks = mutableListOf<GBrowserBookmarks>()
    var myGBrowserHeadersOverwrite = List(0) { GBrowserHeadersOverwrite("", "", "", false) }
  }

  override fun getState(): State = synchronized(lock) {
    return myState
  }

  override fun loadState(state: State) = synchronized(lock) {
    myState = state
  }

}