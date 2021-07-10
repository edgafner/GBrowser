package com.github.gib.actions

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.ClickListener
import com.intellij.ui.SearchTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.UI
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel

class GTextFieldAction(text: String, description: String, icon: Icon, private val jbCefBrowser: JBCefBrowser) :
    AnAction(text, description, icon), CustomComponentAction, DumbAware {
    private var myDescription: String? = null
    private val urlTextField = SearchTextField("GBrowser.History")

    init {
        urlTextField.text = text
        urlTextField.minimumSize = JBDimension(400, 32)
        urlTextField.preferredSize = JBDimension(400, 32)

        urlTextField.addKeyboardListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    e.consume()
                    perform()
                }
            }
        })

        urlTextField.addCurrentTextToHistory()
        urlTextField.setHistorySize(10)

    }


    override fun actionPerformed(e: AnActionEvent) {
        perform()
    }

    fun perform() {
        jbCefBrowser.loadURL(urlTextField.text)
        ActivityTracker.getInstance().inc()
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val panel = UI.PanelFactory.panel(urlTextField).resizeX(true).resizeY(true).createPanel()
        urlTextField.isOpaque = true
        urlTextField.toolTipText = myDescription

        object : ClickListener() {
            override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
                perform()
                return true
            }
        }.installOn(JLabel())
        return panel
    }

    fun setText(value: String) {
        urlTextField.text = value
    }


}