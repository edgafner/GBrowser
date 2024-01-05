package com.github.gbrowser.settings.project


import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class GBrowserProjectSettingsConfigurable : SearchableConfigurable {

  private val projectSettingsComponent = GBrowserProjectSettingsComponent()

  override fun createComponent(): JComponent {
    return projectSettingsComponent.settingsComponent
  }

  override fun getPreferredFocusedComponent(): JComponent {
    return projectSettingsComponent.textField
  }

  override fun isModified(): Boolean {
    return projectSettingsComponent.isModified()
  }

  override fun apply() {
    projectSettingsComponent.apply()
  }

  override fun reset() {
    projectSettingsComponent.reset()
  }

  override fun getDisplayName(): String {
    return "GBrowser"
  }

  override fun getId(): String {
    return "com.github.gbrowser.settings.project.GBrowserProjectSettingsConfigurable"
  }

  override fun disposeUIResources() {
    projectSettingsComponent.dispose()
  }

  fun openCollapseBookMarks(expand: Boolean = false) {
    PropertiesComponent.getInstance().setValue(GBrowserProjectSettingsComponent.BOOKMARKS_OPTIONS_EXPANDED_KEY, expand)

  }
}
