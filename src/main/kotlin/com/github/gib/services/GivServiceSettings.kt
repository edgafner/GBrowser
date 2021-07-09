package com.github.gib.services

import com.intellij.codeInsight.hints.InlayHintsSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jdom.Element
import java.util.*

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

    class State {
        var homePage: String = "https://plugins.jetbrains.com/plugin/14458-gideabrowser"
    }

    override fun getState(): State = synchronized(lock) {
        return myState
    }

    override fun loadState(state: State) = synchronized(lock) {
        myState = state
    }


}