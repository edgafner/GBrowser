package com.github.gib.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.refactoring.introduceParameter.onClickCallback
import com.intellij.ui.InplaceButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.jediterm.terminal.SubstringFinder.FindResult.FindItem
import java.awt.Dimension
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.BasicArrowButton


class FindDialog(val project: Project, private val jbCefBrowser: JBCefBrowser) : DialogWrapper(project, false) {

    private val myFindComponent: SearchPanel

    init {
        title = ""
        isModal = false
        isResizable = false
        setUndecorated(true)
        myFindComponent = createComponent()

        init()
    }

    override fun createCenterPanel(): JComponent {

//        val field: JTextField = JBTextField( "query" , 20)
//        field.addActionListener { e: ActionEvent? -> close(0) }
//
//        field.document.addDocumentListener(object : DocumentListener {
//            override fun insertUpdate(event: DocumentEvent?) {
//                updateFilter(field)
//            }
//
//            override fun removeUpdate(event: DocumentEvent?) {
//                updateFilter(field)
//            }
//
//            override fun changedUpdate(event: DocumentEvent?) {
//                updateFilter(field)
//            }
//        })
//
//        window.addWindowFocusListener(object : WindowAdapter() {
//            override fun windowLostFocus(event: WindowEvent) {
//                jbCefBrowser.cefBrowser.stopFinding(true)
//                close(0)
//            }
//        })
//
//        return field

        return myFindComponent
    }

    fun setDialogLocation() {
        val location = jbCefBrowser.component.locationOnScreen
        val width = jbCefBrowser.component.width

        location.setLocation(location.x + width - myFindComponent.preferredSize.width - 30, location.y)

        setLocation(location)
        setInitialLocationCallback { location }
    }

    private fun findText(text: String, next: Boolean = false) {
        jbCefBrowser.cefBrowser.find(0, text, next, false, true)
    }

    override fun createActions(): Array<Action> {
        return emptyArray()
    }

    private fun createComponent(): SearchPanel {
        val searchPanel = SearchPanel()
        searchPanel.requestFocus()
        searchPanel.addDocumentChangeListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                textUpdated()
            }

            override fun removeUpdate(e: DocumentEvent) {
                textUpdated()
            }

            override fun changedUpdate(e: DocumentEvent) {
                textUpdated()
            }

            private fun textUpdated() {
                findText(myFindComponent.text)
            }
        })
        searchPanel.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(keyEvent: KeyEvent) {
                if (keyEvent.keyCode == KeyEvent.VK_ESCAPE) {
                    close(0)
                } else if (keyEvent.keyCode == KeyEvent.VK_ENTER || keyEvent.keyCode == KeyEvent.VK_UP) {
                    findText(myFindComponent.text, true)
                } else if (keyEvent.keyCode == KeyEvent.VK_DOWN) {
                    findText(myFindComponent.text)
                } else {
                    super.keyPressed(keyEvent)
                }
            }
        })

        return searchPanel
    }


    inner class SearchPanel : JPanel() {
        private val myTextField = JTextField(20)
        private val prev: InplaceButton
        private val next: InplaceButton
        private val ignoreCaseCheckBox = JCheckBox("Ignore Case", true)


        private fun createNextButton(): InplaceButton {
            return InplaceButton(IconButton("Next", AllIcons.General.ChevronDown, AllIcons.General.ArrowDown)) {
                findText(text, true)
            }
        }

        private fun createPrevButton(): InplaceButton {
            return InplaceButton(IconButton("Previous", AllIcons.General.ChevronUp, AllIcons.General.ArrowUp)) {
                findText(text, false)
            }
        }

        val text: String
            get() = myTextField.text

        fun ignoreCase(): Boolean {
            return ignoreCaseCheckBox.isSelected
        }

        override fun requestFocus() {
            myTextField.requestFocus()
        }

        fun addDocumentChangeListener(listener: DocumentListener) {
            myTextField.document.addDocumentListener(listener)
        }

        override fun addKeyListener(listener: KeyListener) {
            myTextField.addKeyListener(listener)
        }

        fun addIgnoreCaseListener(listener: ItemListener) {
            ignoreCaseCheckBox.addItemListener(listener)
        }

        init {
            next = createNextButton()
            prev = createPrevButton()

            myTextField.isEditable = true
            add(myTextField)
//            add(ignoreCaseCheckBox)
            add(next)
            add(prev)
            isOpaque = true
        }
    }
}