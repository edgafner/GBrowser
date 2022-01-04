package com.github.gib

import com.github.gib.actions.*
import com.github.gib.gcef.GBCefBrowser
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase.ErrorPage
import org.cef.handler.CefLoadHandler
import javax.swing.ImageIcon


@Suppress("UnstableApiUsage")
class GivMainPanel(private val initialUrl: String) : SimpleToolWindowPanel(true, true), Disposable {

    private val jbCefBrowser: JBCefBrowser = GBCefBrowser(initialUrl)

    init {
        val toolbar =
            ActionManager.getInstance().createActionToolbar(ActionPlaces.CONTEXT_TOOLBAR, buildToolbar(), true).apply {
                targetComponent = this@GivMainPanel
            }

        jbCefBrowser.setErrorPage { errorCode, errorText, failedUrl ->
            if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) null else ErrorPage.DEFAULT.create(errorCode,
                errorText,
                failedUrl)
        }
        jbCefBrowser.setProperty(JBCefBrowser.Properties.FOCUS_ON_SHOW, true)
        jbCefBrowser.setProperty(JBCefBrowser.Properties.FOCUS_ON_NAVIGATION, true)

        setContent(jbCefBrowser.component)
        setToolbar(toolbar.component)


    }

    private fun buildToolbar(): DefaultActionGroup {
        val toolbar = DefaultActionGroup()
        val backButton = GBackAction(jbCefBrowser, ImageIcon(javaClass.getResource("/actions/back.png")))
        val forwardButton = GForwardAction(jbCefBrowser, ImageIcon(javaClass.getResource("/actions/forward.png")))
        val refreshButton = GRefreshAction(jbCefBrowser, ImageIcon(javaClass.getResource("/actions/refresh.png")))
        val homeButton = GHomeAction(jbCefBrowser, AllIcons.Nodes.HomeFolder)
        val bookMarkFavorites = GFavoritesMenuAction(jbCefBrowser)
        val gCustomizeActionGroup = GCustomizeActionGroup(jbCefBrowser)


        val bus = ApplicationManager.getApplication().messageBus
        bus.connect().subscribe(SettingsChangedAction.TOPIC, object : SettingsChangedAction {
            override fun settingsChanged() {
                bookMarkFavorites.updateView()
            }
        })

        val urlTextField = GSearchFieldAction(initialUrl,
            "Web address",
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
        toolbar.addSeparator()
        toolbar.add(gCustomizeActionGroup, Constraints.LAST)

        return toolbar
    }


    override fun dispose() {
        jbCefBrowser.dispose()
    }
}
