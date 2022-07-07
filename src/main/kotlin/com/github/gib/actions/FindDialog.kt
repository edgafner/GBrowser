package com.github.gib.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.ui.InplaceButton
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.event.ItemListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class FindDialog(val project: Project, private val jbCefBrowser: JBCefBrowser) :
    DialogWrapper(project, false, false) {

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
        return myFindComponent
    }

    /**
     * This to disable the ok and cancel
     */
    override fun createActions(): Array<Action> {
        return emptyArray()
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
                when (keyEvent.keyCode) {
                    KeyEvent.VK_ESCAPE -> {
                        myFindComponent.close()
                        close(0)
                    }

                    KeyEvent.VK_ENTER, KeyEvent.VK_UP -> {
                        findText(myFindComponent.text, true)
                    }

                    KeyEvent.VK_DOWN -> {
                        findText(myFindComponent.text)
                    }

                    else -> {
                        super.keyPressed(keyEvent)
                    }
                }
            }
        })

        return searchPanel
    }

    inner class SearchPanel : JPanel() {
        private val myTextField = JTextField(15)
        private val prev: InplaceButton
        private val next: InplaceButton
        private val close: InplaceButton
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

        private fun createCloseButton(): InplaceButton {
            return InplaceButton(IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered)) {
                close()
            }
        }

        fun close() {
            jbCefBrowser.cefBrowser.stopFinding(true)
            close(0)
        }

        val text: String
            get() = myTextField.text

        @Suppress("unused")
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

        @Suppress("unused")
        fun addIgnoreCaseListener(listener: ItemListener) {
            ignoreCaseCheckBox.addItemListener(listener)
        }

        init {
            next = createNextButton()
            prev = createPrevButton()
            close = createCloseButton()
            myTextField.isEditable = true
            add(myTextField)
//            add(ignoreCaseCheckBox)
            add(next)
            add(prev)
            add(close)
            isOpaque = true
        }

    }
}