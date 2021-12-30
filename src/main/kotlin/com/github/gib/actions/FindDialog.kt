package com.github.gib.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class FindDialog(val project: Project, private val jbCefBrowser: JBCefBrowser) : DialogWrapper(project, false) {


    init {
        title = "";
        isModal = false;
        isResizable = false;
        setUndecorated(true);
    }

    override fun createCenterPanel(): JComponent {

        val field: JTextField = JBTextField( "query" , 20)
        field.addActionListener { e: ActionEvent? -> close(0) }

        field.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(event: DocumentEvent?) {
                updateFilter(field)
            }

            override fun removeUpdate(event: DocumentEvent?) {
                updateFilter(field)
            }

            override fun changedUpdate(event: DocumentEvent?) {
                updateFilter(field)
            }
        })

        window.addWindowFocusListener(object : WindowAdapter() {
            override fun windowLostFocus(event: WindowEvent) {
                jbCefBrowser.cefBrowser.stopFinding(true)
                close(0)
            }
        })

        return field

    }

    fun setDialogLocation(event: AnActionEvent) {
        val location = event.inputEvent.component.mousePosition
        SwingUtilities.convertPointToScreen(location, event.inputEvent.component)
        setLocation(location)
        setInitialLocationCallback { location }
    }

    private fun updateFilter(textField: JTextField) {
        if (Objects.isNull(jbCefBrowser)) {
            return
        }
        jbCefBrowser.cefBrowser.find(0, textField.text, true, false, false)

    }




}