package com.github.gib

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextField


@Suppress("UnstableApiUsage")
class GivMainPanel : SimpleToolWindowPanel(true, true), Disposable {

    /**
     * Maybe change this to something else
     */
    private val url = "https://youtube.com"

    init {
        initGivPanel()
    }

    private fun initGivPanel() {
        val jbCefBrowser = JBCefBrowser(url)
//        val myDevTools: CefBrowser = jbCefBrowser.getCefBrowser().getDevTools()
//        val myDevToolsBrowser = JBCefBrowser(myDevTools, jbCefBrowser.getJBCefClient())
//        myDevToolsBrowser.age

        val divPanel = JPanel(BorderLayout())
        divPanel.border = IdeBorderFactory.createBorder(UIUtil.getBoundsColor(), SideBorder.ALL)
        divPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        setContent(divPanel)

        val myUrlBar = JTextField(url)
        val panel = panel {
            row { myUrlBar() }
        }

        myUrlBar.addActionListener { jbCefBrowser.loadURL(myUrlBar.text) }
        divPanel.add(panel, BorderLayout.NORTH)
    }

    override fun dispose() {}
}
