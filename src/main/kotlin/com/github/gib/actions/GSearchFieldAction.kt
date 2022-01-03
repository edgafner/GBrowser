package com.github.gib.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.ui.SearchTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StartupUiUtil
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.event.KeyEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.Border
import javax.swing.border.CompoundBorder


class GSearchFieldAction(
    text: String,
    description: String,
    icon: Icon,
    private val jbCefBrowser: JBCefBrowser,
) : AnAction(text, description, icon),
    CustomComponentAction {
    private val panel: JPanel = JPanel()
    private val urlTextField: SearchTextField = object : SearchTextField(true) {
        override fun preprocessEventForTextField(e: KeyEvent): Boolean {
            if (KeyEvent.VK_ENTER == e.keyCode) {
                e.consume()
                addCurrentTextToHistory()
                perform()
            }
            return super.preprocessEventForTextField(e)
        }


        override fun onFieldCleared() {
        }
    }

    private fun perform() {
        jbCefBrowser.loadURL(urlTextField.text)
        actionPerformed(ActionUtil.createEmptyEvent())
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return panel
    }


    fun setText(value: String) {
        urlTextField.text = value
    }

    init {
        urlTextField.setHistorySize(10)

        val border = urlTextField.border
        val emptyBorder: Border = JBUI.Borders.empty(3, 0, 2, 0)
        if (border is CompoundBorder) {
            if (!StartupUiUtil.isUnderDarcula()) {
                urlTextField.border = CompoundBorder(emptyBorder, border.insideBorder)
            }
        } else {
            urlTextField.border = emptyBorder
        }


        panel.apply {
            isOpaque = false
            layout = MigLayout(LC().gridGap("0", "0").insets("0", "0", "0", "0").fillX())
            panel.add(urlTextField, CC().width("320").pushX().growX())
        }
        panel.preferredSize = JBDimension(350, 32)
    }

    override fun actionPerformed(e: AnActionEvent) {

    }


}