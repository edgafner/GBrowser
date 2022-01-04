package com.github.gib.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.Pair
import com.intellij.util.ui.JBImageIcon
import java.net.URL
import javax.imageio.ImageIO

@State(name = "GivServiceSettings", storages = [Storage("editor.xml"), Storage("workspace.xml", deprecated = true)])
class GivServiceSettings : PersistentStateComponent<GivServiceSettings.State> {

    private val lock = Any()
    private var myState = State()


    companion object {
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

    fun getFavorites(): MutableList<Pair<String, JBImageIcon>> {
        return myState.favorites
    }


    fun addToFavorites(webToFavorite: List<String>) = synchronized(lock) {
        myState.favorites.removeAll { true }
        webToFavorite.forEach {
            myState.favorites.add(Pair.create(it,
                JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=$it")))))
        }
    }

    @Suppress("unused")
    fun addFavorite(webToFavorite: String) = synchronized(lock) {
        myState.favorites.add(Pair.create(webToFavorite,
            JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=$webToFavorite")))))

    }

    class State {
        var homePage: String = "https://plugins.jetbrains.com/plugin/18269-queryflag"
        val favorites = mutableListOf<Pair<String, JBImageIcon>>()
    }

    override fun getState(): State = synchronized(lock) {
        return myState
    }

    override fun loadState(state: State) = synchronized(lock) {
        myState = state
    }
}