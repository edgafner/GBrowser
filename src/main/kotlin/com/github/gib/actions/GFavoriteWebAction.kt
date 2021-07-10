package com.github.gib.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBImageIcon
import java.net.URL
import javax.imageio.ImageIO

class GFavoriteWebAction(private val webUrl: String, icon: JBImageIcon, private val jbCefBrowser: JBCefBrowser) :
    AnAction(icon), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        jbCefBrowser.cefBrowser.loadURL(webUrl)
    }

}
