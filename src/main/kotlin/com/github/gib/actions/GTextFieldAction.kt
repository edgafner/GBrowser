package com.github.gib.actions

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.ClickListener
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder

class GTextFieldAction(text: String, description: String, icon: Icon, private val jbCefBrowser: JBCefBrowser) :
    AnAction(text, description, icon), CustomComponentAction, DumbAware {
    private var myDescription: String? = null
    private val urlTextField = JTextField(text)

    init {
        urlTextField.minimumSize = JBDimension(400,32)
        urlTextField.preferredSize = JBDimension(400,32)
        urlTextField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    e.consume()
                    perform()
                }
            }
        })
    }


    override fun actionPerformed(e: AnActionEvent) {
        perform()
    }

    fun perform() {
        jbCefBrowser.loadURL(urlTextField.text)
        ActivityTracker.getInstance().inc()
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        // honestly borrowed from SearchTextField
        val panel = JPanel(BorderLayout())

        urlTextField.isOpaque = true
        panel.add(urlTextField, BorderLayout.WEST)
        urlTextField.toolTipText = myDescription
        val originalBorder: Border = if (SystemInfo.isMac) {
            BorderFactory.createLoweredBevelBorder()
        } else {
            urlTextField.border
        }
        panel.border = CompoundBorder(JBUI.Borders.empty(4, 0, 4, 0), originalBorder)
        urlTextField.isOpaque = true
        urlTextField.border = JBUI.Borders.empty(0, 5, 0, 5)
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