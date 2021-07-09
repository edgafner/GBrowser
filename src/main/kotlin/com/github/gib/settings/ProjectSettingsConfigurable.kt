package com.github.gib.settings

import com.github.gib.services.GivServiceSettings
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class ProjectSettingsConfigurable : SearchableConfigurable {

    private var projectSettingsComponent : ProjectSettingsComponent? = null

    override fun createComponent(): JComponent {

        projectSettingsComponent = ProjectSettingsComponent()
        return projectSettingsComponent!!.getPanel()
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return projectSettingsComponent!!.getPreferredFocusedComponent()
    }

    override fun isModified(): Boolean {
        val settings: GivServiceSettings = GivServiceSettings.instance()
        return  projectSettingsComponent!!.getHomePageText() != settings.getLastSaveHomePage()
    }

    override fun apply() {
        GivServiceSettings.instance().saveHomePage(projectSettingsComponent!!.getHomePageText())
    }

    override fun reset() {
        projectSettingsComponent!!.setHomePageText(GivServiceSettings.instance().getLastSaveHomePage())

    }

    override fun getDisplayName(): String {
        return "GIdea Embedded Browser"
    }

    override fun getId(): String {
        return "com.github.gib.settings.ProjectSettingsConfigurable"
    }

    override fun disposeUIResources() {
        projectSettingsComponent = null
    }

}