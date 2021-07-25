package com.github.gib.actions

import com.github.gib.services.GivServiceSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser

class GFavoritesMenuAction : DefaultActionGroup(), DumbAware {


    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = childrenCount <= 0
        e.presentation.icon = AllIcons.Nodes.Favorite

    }

    fun updateView(settings: GivServiceSettings, jbCefBrowser: JBCefBrowser) {
        removeAll()
        addAll(settings.getFavorites().map { GFavoriteWebAction(it.first, it.second, jbCefBrowser) })
    }

}




