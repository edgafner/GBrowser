package com.github.gib.services

import com.github.gib.settings.HeadersOverwrite
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Pair
import com.intellij.util.ui.JBImageIcon
import java.net.URL
import javax.imageio.ImageIO

@State(name = "GivServiceSettings", storages = [Storage("gideabrowser.xml")])
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

    fun getFavorites(): MutableList<Pair<String, JBImageIcon>> {
        return myState.favorites
    }

    fun getHeadersOverwrite(): List<HeadersOverwrite> {
        return myState.headersOverwrite
    }


    fun addToFavorites(webToFavorite: List<String>) = synchronized(lock) {
        myState.favorites.removeAll { true }
        webToFavorite.forEach {
            try {
                val imageIo = JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=$it")))
                myState.favorites.add(
                    Pair.create(
                        it,
                        imageIo
                    )
                )
            } catch (e: Exception) {
                LOG.warn("Error adding favorite $it", e)
            }
        }
    }

    fun addFavorite(webToFavorite: String) = synchronized(lock) {
        try {
            val imageIo = ImageIO.read(URL("https://www.google.com/s2/favicons?domain=$webToFavorite"))
            myState.favorites.add(
                Pair.create(
                    webToFavorite,
                    JBImageIcon(imageIo)
                )
            )
        } catch (e: Exception) {
            LOG.warn("Error adding favorite", e)
        }
    }

    fun addToHeadersOverwrite(headersOverwrite: List<HeadersOverwrite>) = synchronized(lock) {
        myState.headersOverwrite = headersOverwrite
    }

    class State {
        var homePage: String = "https://www.dorkag.com/dorkag"
        val favorites = mutableListOf<Pair<String, JBImageIcon>>()
        var headersOverwrite = List(0) { HeadersOverwrite("", "", "", false) }
    }

    override fun getState(): State = synchronized(lock) {
        return myState
    }

    override fun loadState(state: State) = synchronized(lock) {
        myState = state
    }

}