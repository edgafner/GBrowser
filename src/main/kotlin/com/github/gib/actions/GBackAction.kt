package com.github.gib.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.Icon
import javax.swing.ImageIcon

class GBackAction(private val jbCefBrowser: JBCefBrowser, icon : Icon) : AnAction(icon) {


    override fun update(e: AnActionEvent) {
        if (!jbCefBrowser.cefBrowser.canGoBack()) {
            e.presentation.isEnabled = false
            return
        }
        super.update(e)

    }

    override fun actionPerformed(e: AnActionEvent) {
        jbCefBrowser.cefBrowser.goBack()
    }
}