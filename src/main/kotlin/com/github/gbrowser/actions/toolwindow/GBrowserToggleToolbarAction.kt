package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.ui.toolwindow.gbrowser.getContentManager
import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.ide.actions.ToggleToolbarAction
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindow
import javax.swing.JComponent

class GBrowserToggleToolbarAction : AnAction(), DumbAware {


  override fun update(e: AnActionEvent) {
    val contentManager = getContentManager(e.project, GBrowserUtil.GROUP_DISPLAY_ID)
    e.presentation.isEnabled = contentManager?.isEmpty?.not() ?: false
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  fun getShowToolbarProperty(window: ToolWindow): String {
    return "ToolWindow${window.stripeTitle}.ShowToolbar"
  }

  fun setToolbarVisibleImpl(property: String,
                            propertiesComponent: PropertiesComponent,
                            components: Iterable<JComponent>,
                            visible: Boolean) {
    propertiesComponent.setValue(property, visible.toString(), "true")
    ToggleToolbarAction.setToolbarVisible(components, visible)
  }

  override fun actionPerformed(e: AnActionEvent) {
    getSelectedBrowserPanel(e)?.let { panel ->
      val isVisible = panel.isToolBarVisible()
      panel.setToolBarVisible(!isVisible)
    }
  }
}
