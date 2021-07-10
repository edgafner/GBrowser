package com.github.gib

import com.github.gib.actions.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.ImageIcon


@Suppress("UnstableApiUsage")
class GivMainPanel(private val initialUrl: String) : SimpleToolWindowPanel(true, true),
    Disposable {

    private val jbCefBrowser: JBCefBrowser = JBCefBrowser(initialUrl)

    init {
        toolbar = ActionManager.getInstance()
            .createActionToolbar(ActionPlaces.MAIN_TOOLBAR, buildToolbar(), true).component
        setContent(jbCefBrowser.component)

    }

    private fun buildToolbar(): DefaultActionGroup {
        val toolbar = DefaultActionGroup()
        val backButton = GBackAction(jbCefBrowser, ImageIcon(javaClass.getResource("/actions/back.png")))
        val forwardButton = GForwardAction(jbCefBrowser, ImageIcon(javaClass.getResource("/actions/forward.png")))
        val refreshButton = GRefreshAction(jbCefBrowser, ImageIcon(javaClass.getResource("/actions/refresh.png")))
        val homeButton = GHomeAction(jbCefBrowser, AllIcons.Nodes.HomeFolder)
        val bookMarkFavorites = GFavoritesMenuAction()
        bookMarkFavorites.jbCefBrowser = jbCefBrowser


        val urlTextField = GTextFieldAction(initialUrl, "Web address",
            ImageIcon(javaClass.getResource("/actions/refresh.png")),
            jbCefBrowser)


        jbCefBrowser.cefBrowser.client.addDisplayHandler(CefUrlChangeHandler { url -> urlTextField.setText(url ?: "") })

        toolbar.add(backButton)
        toolbar.add(forwardButton)
        toolbar.add(refreshButton)
        toolbar.add(homeButton)
        toolbar.add(bookMarkFavorites)
        toolbar.addSeparator()
        toolbar.add(urlTextField)

        return toolbar
    }


    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
