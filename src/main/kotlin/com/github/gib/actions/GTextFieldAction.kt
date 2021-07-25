package com.github.gib.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.SearchTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBDimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.Icon
import javax.swing.JComponent

class GTextFieldAction(text: String, private val description: String,icon: Icon, private val jbCefBrowser: JBCefBrowser) :
    AnAction(text, description, icon), CustomComponentAction, DumbAware {
    private val urlTextField = SearchTextField("GBrowser.History")

    init {
        urlTextField.text = text
        urlTextField.minimumSize = JBDimension(450, 32)
        urlTextField.preferredSize = JBDimension(450, 32)

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
//        perform()
    }

    fun perform() {
        jbCefBrowser.loadURL(urlTextField.text)
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        // honestly borrowed from SearchTextField
        return urlTextField
    }

    fun setText(value: String) {
        urlTextField.text = value
    }


}