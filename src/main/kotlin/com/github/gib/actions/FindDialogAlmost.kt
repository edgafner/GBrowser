package com.github.gib.actions

import com.intellij.icons.AllIcons
import com.intellij.ide.plugins.newui.HorizontalLayout
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.ui.InplaceButton
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBUI
import java.awt.event.ItemListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class FindDialogAlmost(val project: Project, private val jbCefBrowser: JBCefBrowser) :
    DialogWrapper(project, false, false) {


    private val myFindComponent: OnePixelSplitter

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

    private fun createComponent(): OnePixelSplitter {
        val searchPanel = SearchPanel()
        val jTextField = JTextField(20)
        jTextField.isEditable = true
        searchPanel.setContent(jTextField)
        searchPanel.requestFocus()
        val mySplitter = OnePixelSplitter(false)

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
                findText(searchPanel.text)
            }
        })
        searchPanel.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(keyEvent: KeyEvent) {
                if (keyEvent.keyCode == KeyEvent.VK_ESCAPE) {
                    close()
                    close(0)
                } else if (keyEvent.keyCode == KeyEvent.VK_ENTER || keyEvent.keyCode == KeyEvent.VK_UP) {
                    findText(searchPanel.text, true)
                } else if (keyEvent.keyCode == KeyEvent.VK_DOWN) {
                    findText(searchPanel.text)
                } else {
                    super.keyPressed(keyEvent)
                }
            }
        })

        val rightPanel: JPanel = NonOpaquePanel(HorizontalLayout(JBUI.scale(1)))
        rightPanel.add(createNextButton(searchPanel))
        rightPanel.add(createPrevButton(searchPanel))
        rightPanel.add(createCloseButton())
        mySplitter.firstComponent = searchPanel
        mySplitter.secondComponent = rightPanel
        return mySplitter
    }

    private fun createNextButton(searchPanel: SearchPanel): InplaceButton {
        return InplaceButton(IconButton("Next", AllIcons.General.ChevronDown, AllIcons.General.ArrowDown)) {
            findText(searchPanel.text, true)
        }
    }

    private fun createPrevButton(searchPanel: SearchPanel): InplaceButton {
        return InplaceButton(IconButton("Previous", AllIcons.General.ChevronUp, AllIcons.General.ArrowUp)) {
            findText(searchPanel.text, false)
        }
    }

    private fun createCloseButton(): InplaceButton {
        return InplaceButton(IconButton("Close", AllIcons.Actions.Close, AllIcons.Actions.CloseHovered)) {
            close()
        }
    }

    private fun close() {
        jbCefBrowser.cefBrowser.stopFinding(true)
        close(0)
    }

    inner class SearchPanel : Wrapper() {


        private val ignoreCaseCheckBox = JCheckBox("Ignore Case", true)


        val text: String
            get() = (targetComponent as JTextField).text

        fun ignoreCase(): Boolean {
            return ignoreCaseCheckBox.isSelected
        }

        override fun requestFocus() {
            targetComponent.requestFocus()
        }

        fun addDocumentChangeListener(listener: DocumentListener) {
            (targetComponent as JTextField).document.addDocumentListener(listener)
        }

        override fun addKeyListener(listener: KeyListener) {
            (targetComponent as JTextField).addKeyListener(listener)
        }

        fun addIgnoreCaseListener(listener: ItemListener) {
            ignoreCaseCheckBox.addItemListener(listener)
        }

//        init {
//
//            myTextField.isEditable = true
//            add(myTextField)
//            isOpaque = true
//        }

    }
}

