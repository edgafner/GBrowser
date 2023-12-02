package com.github.gbrowser.actions

import com.github.gbrowser.GCookieManagerDialog
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.GBrowserMainPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.Icon

var zoomLevel = 0.0

class GZoomOutAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) :
  AnAction(GBrowserBundle.message("actions.zoom.out.text"), "", icon), DumbAware {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    jbCefBrowser.cefBrowser.zoomLevel = --zoomLevel
  }
}

class GZoomInAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) :
  AnAction(GBrowserBundle.message("actions.zoom.in.text"), "", icon), DumbAware {


  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    jbCefBrowser.cefBrowser.zoomLevel = ++zoomLevel


  }
}

class GFindAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) : AnAction("Find...", "", icon), DumbAware {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = true
  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    val findDialog = FindDialog(e.project!!, jbCefBrowser)

    if (!findDialog.isVisible) {
      findDialog.setDialogLocation()
    }

    findDialog.show()
  }
}

@Suppress("ComponentNotRegistered", "unused")
class GCookiesAction(private val jbCefBrowser: JBCefBrowser,
                     icon: Icon,
                     private val gbrowserMainPanel: GBrowserMainPanel) : AnAction("Cookie Manager", "", icon) {

  override fun actionPerformed(e: AnActionEvent) {
    val myCookieManagerDialog = GCookieManagerDialog(gbrowserMainPanel, jbCefBrowser)
    if (myCookieManagerDialog.showAndGet()) {
      val cookies = jbCefBrowser.jbCefCookieManager.getCookies(null, false).get()
      if (cookies.isNotEmpty()) {
        myCookieManagerDialog.update(cookies)
      }
    }
  }
}

