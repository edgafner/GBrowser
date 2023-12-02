package com.github.gbrowser.services

import com.github.gbrowser.settings.FavoritesWeb
import com.github.gbrowser.settings.HeadersOverwrite
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger

@State(name = "GivServiceSettings", storages = [Storage(value = "gbrowser.xml")], category = SettingsCategory.TOOLS)
class GivServiceSettings : PersistentStateComponent<GivServiceSettings.State> {

  private val lock = Any()
  private var myState = State()

  companion object {
    private val LOG = logger<GivServiceSettings>()

    @JvmStatic
    fun instance(): GivServiceSettings {
      return ApplicationManager.getApplication().getService(GivServiceSettings::class.java)
    }
  }


  fun saveHomePage(homePage: String) = synchronized(lock) {
    myState.homePage = homePage
  }

  fun getLastSaveHomePage(): String {
    return myState.homePage
  }

  fun getFavorites(): MutableList<FavoritesWeb> {
    return myState.favorites
  }

  fun getHeadersOverwrite(): List<HeadersOverwrite> {
    return myState.headersOverwrite
  }


  fun addToFavorites(webToFavorite: List<FavoritesWeb>) = synchronized(lock) {
    myState.favorites.removeAll { true }
    try {
      myState.favorites.addAll(webToFavorite)
    }
    catch (e: Exception) {
      LOG.warn("Error adding favorite", e)
    }
  }

  fun addFavorite(webToFavorite: FavoritesWeb) = synchronized(lock) {
    try {
      myState.favorites.add(webToFavorite)
    }
    catch (e: Exception) {
      LOG.warn("Error adding favorite", e)
    }
  }

  fun addToHeadersOverwrite(headersOverwrite: List<HeadersOverwrite>) = synchronized(lock) {
    myState.headersOverwrite = headersOverwrite
  }

  class State {
    var homePage: String = "https://www.dorkag.com/dorkag"
    var favorites = mutableListOf<FavoritesWeb>()
    var headersOverwrite = List(0) { HeadersOverwrite("", "", "", false) }
  }

  override fun getState(): State = synchronized(lock) {
    return myState
  }

  override fun loadState(state: State) = synchronized(lock) {
    myState = state
  }

}