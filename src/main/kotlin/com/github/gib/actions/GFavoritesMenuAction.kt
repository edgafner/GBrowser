package com.github.gib.actions

import com.github.gib.services.GivServiceSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser

class GFavoritesMenuAction : DefaultActionGroup(), DumbAware {

    var jbCefBrowser: JBCefBrowser? = null


    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = childrenCount <= 0
        e.presentation.icon = AllIcons.Nodes.Favorite
        val instance = GivServiceSettings.instance()
        if (jbCefBrowser != null) {
            updateView(instance)
        }

    }

    private fun updateView(settings: GivServiceSettings) {
        removeAll()
        addAll(settings.getFavorites().map { GFavoriteWebAction(it.first, it.second, jbCefBrowser!!) })
    }

}




