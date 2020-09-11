package com.github.gib

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.layout.panel
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField

@Suppress("UnstableApiUsage")
class GivMainPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    //simple comment
    private val URL = "http://youtube.com"

    init {
        when (JBCefApp.isSupported()) {
            true -> initGivPanel()
            else -> notifyDisable()
        }
    }

    private fun initGivPanel() {
        val jbCefBrowser = JBCefBrowser(URL)

        val divPanel = JPanel(BorderLayout())
        divPanel.border = IdeBorderFactory.createBorder(UIUtil.getBoundsColor(), SideBorder.ALL)
        divPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        setContent(divPanel)


        val myUrlBar = JTextField(URL)
        val panel = panel {
            row { myUrlBar() }
        }

        myUrlBar.addActionListener { event: ActionEvent? -> jbCefBrowser.loadURL(myUrlBar.text) }

        divPanel.add(panel, BorderLayout.NORTH)


    }

    private fun notifyDisable() {
        JBPopupFactory.getInstance().createComponentPopupBuilder(
                JTextArea("Set the reg key to enable JCEF:\n\"ide.browser.jcef.enabled=true\""), null
        ).setTitle("JCEF Web Browser Is not Supported").createPopup().showCenteredInCurrentWindow(project)
    }

    override fun dispose() {}
}
