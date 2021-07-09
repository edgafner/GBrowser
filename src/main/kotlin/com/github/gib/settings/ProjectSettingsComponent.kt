package com.github.gib.settings

import com.github.gib.services.GivServiceSettings
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel


class ProjectSettingsComponent {

    private val myMainPanel : JPanel
    private val homePageText = JBTextField(GivServiceSettings.instance().getLastSaveHomePage())

    init {

        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("GIdea default home page"), homePageText, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPanel(): JPanel {
        return myMainPanel
    }

    fun getPreferredFocusedComponent(): JComponent {
        return homePageText
    }

    fun getHomePageText(): String {
        return homePageText.text
    }

    fun setHomePageText( newText: String?) {
        homePageText.setText(newText)
    }


}