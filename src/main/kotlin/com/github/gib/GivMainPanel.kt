package com.github.gib

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SideBorder
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

    /**
     * Maybe change this to something else
     */
    private val url = "http://youtube.com"

    init {
        initGivPanel()
    }

    private fun initGivPanel() {
        val jbCefBrowser = JBCefBrowser(url)

        val divPanel = JPanel(BorderLayout())
        divPanel.border = IdeBorderFactory.createBorder(UIUtil.getBoundsColor(), SideBorder.ALL)
        divPanel.add(jbCefBrowser.component, BorderLayout.CENTER)
        setContent(divPanel)

        val myUrlBar = JTextField(url)
        val panel = panel {
            row { myUrlBar() }
        }

        myUrlBar.addActionListener { event: ActionEvent? -> jbCefBrowser.loadURL(myUrlBar.text) }
        divPanel.add(panel, BorderLayout.NORTH)
    }

    private fun notifyDisable() {
        val jTextArea = JTextArea("Set the reg key to enable JCEF:\n\"ide.browser.jcef.enabled=true\"")
        JBPopupFactory.getInstance().createComponentPopupBuilder(jTextArea, null)
            .setTitle("JCEF Web Browser Is not Supported").createPopup().showCenteredInCurrentWindow(project)
    }

    override fun dispose() {}
}
