package com.github.gib.actions

import com.github.gib.GIdeaBrowserBundle
import com.github.gib.GivToolWindowFactory
import com.intellij.designer.DesignerBundle
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.Icon

var zoomLevel = 0.0

class GZoomOutAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) :
    AnAction(GIdeaBrowserBundle.message("actions.zoom.out.text"), "", icon), DumbAware {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        jbCefBrowser.cefBrowser.zoomLevel = --zoomLevel
    }
}

class GZoomInAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) :
    AnAction(GIdeaBrowserBundle.message("actions.zoom.in.text"), "", icon), DumbAware {


    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        jbCefBrowser.cefBrowser.zoomLevel = ++zoomLevel


    }
}

class GFindAction(private val jbCefBrowser: JBCefBrowser, icon: Icon) : AnAction("Find...", "", icon), DumbAware {

    private var findDialog: FindDialog? = null
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {

        if (findDialog != null && findDialog!!.isShowing()) {
            findDialog!!.close(0);
            return;
        }

        findDialog = FindDialog(e.project!!,jbCefBrowser)
        findDialog!!.setDialogLocation()
        findDialog!!.show()

    }


}