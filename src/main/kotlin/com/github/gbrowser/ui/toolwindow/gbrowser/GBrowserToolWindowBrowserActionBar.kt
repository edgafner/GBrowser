package com.github.gbrowser.ui.toolwindow.gbrowser


import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import javax.swing.JComponent


fun createToolBarAction(actions: AnAction, targetComponent: JComponent?): ActionToolbar {
  val actionGroup = DefaultActionGroup(actions)
  val actionManager = ActionManager.getInstance()
  val actionToolbar = actionManager.createActionToolbar("ToolwindowToolbar", actionGroup, true)
  actionToolbar.layoutPolicy = ActionToolbar.NOWRAP_LAYOUT_POLICY
  actionToolbar.targetComponent = targetComponent
  actionToolbar.component.maximumSize = JBUI.size(Integer.MAX_VALUE, Integer.MAX_VALUE)
  return actionToolbar
}

fun registerAction(actionId: String, actions: AnAction) {
  val actionManager = ActionManager.getInstance()
  actionManager.replaceAction(actionId, actions)
}

fun replaceRegisteredAction(actionId: String, actions: AnAction) {
  val actionManager = ActionManager.getInstance()
  actionManager.replaceAction(actionId, actions)
}

fun unregisterAction(actionId: String) {
  val actionManager = ActionManager.getInstance()
  actionManager.unregisterAction(actionId)
}

fun createToolBarActionPanel(actions: DefaultActionGroup): BorderLayoutPanel {
  val actionToolbar = createToolbar(actions) as ActionToolbar
  val component = actionToolbar.component
  val panel = BorderLayoutPanel()
  panel.addToTop(component)
  panel.isOpaque = false
  return panel
}

fun createToolbar(vararg actions: AnAction): ActionToolbarImpl {
  val toolbar = ActionManager.getInstance().createActionToolbar("ToolwindowToolbar", DefaultActionGroup(*actions), true) as ActionToolbarImpl
  toolbar.setForceMinimumSize(false)
  toolbar.adjustTheSameSize(true)
  toolbar.isOpaque = false
  toolbar.targetComponent = toolbar
  toolbar.layoutPolicy = ActionToolbar.NOWRAP_LAYOUT_POLICY
  toolbar.border = JBUI.Borders.empty()
  return toolbar
}
