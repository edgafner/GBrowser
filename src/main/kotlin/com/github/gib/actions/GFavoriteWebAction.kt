package com.github.gib.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.Icon


class GFavoriteWebAction(private val webUrl: String, icon: Icon, private val jbCefBrowser: JBCefBrowser) :
    AnAction(icon) {

    override fun actionPerformed(e: AnActionEvent) {
        jbCefBrowser.cefBrowser.loadURL(webUrl)
    }

}
