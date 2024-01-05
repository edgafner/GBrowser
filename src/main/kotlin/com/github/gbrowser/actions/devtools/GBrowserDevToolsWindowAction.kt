package com.github.gbrowser.actions.devtools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.ui.gcef.impl.GBrowserCefKeyBordHandler
import com.github.gbrowser.ui.toolwindow.gbrowser.getSelectedBrowserPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Rectangle
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.Icon
import javax.swing.JDialog
import javax.swing.SwingUtilities

class GBrowserDevToolsWindowAction : AnAction() {
  private val icon: Icon = GBrowserIcons.DEV_TOOLS
  private val iconActive: Icon = GBrowserIcons.DEV_TOOLS_ACTIVE
  private var devToolDialog: JDialog? = null


  override fun update(e: AnActionEvent) {
    val panel = getSelectedBrowserPanel(e)
    e.presentation.isEnabled = panel?.hasContent() ?: false
    e.presentation.icon = if (devToolDialog != null) iconActive else icon
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    if (devToolDialog != null) {
      devToolDialog?.isVisible = true
      devToolDialog?.toFront()
    } else {
      getSelectedBrowserPanel(e)?.let { panel ->
        val devTools = panel.createDevTools(panel.getCurrentUrl())
        panel.getBrowser().component.let { component ->
          val ancestor = SwingUtilities.getWindowAncestor(component as Component)
          val bounds = ancestor.graphicsConfiguration.bounds
          val dialogBounds = Rectangle(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2)

          devToolDialog = JDialog(ancestor).apply {
            title = GBrowserBundle.message("actions.devTools.open.text", emptyArray<String>())
            defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
            setBounds(dialogBounds)
            layout = BorderLayout()

            devTools?.component?.let {
              add(it as Component, BorderLayout.CENTER)
            }

            addWindowListener(object : WindowAdapter() {
              override fun windowActivated(e: WindowEvent?) {
                devTools?.cefBrowser?.client?.apply {
                  removeKeyboardHandler()
                  addKeyboardHandler(GBrowserCefKeyBordHandler())
                }
              }

              override fun windowDeactivated(e: WindowEvent?) {
                devTools?.cefBrowser?.client?.removeKeyboardHandler()
              }

              override fun windowClosing(e: WindowEvent?) {
                devTools?.disposeDevTools()
                devToolDialog = null
              }
            })

            isVisible = true
          }
        }
        panel.setDevToolsBrowser(devTools)
      }
    }
  }
}

