package com.github.gbrowser.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.Icon

class GForwardAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) : AnAction(icon), DumbAware {

  override fun update(e: AnActionEvent) {
    if (!jbCefBrowser.cefBrowser.canGoForward()) {
      e.presentation.isEnabled = false
      return
    }
  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    jbCefBrowser.cefBrowser.goForward()
  }
}