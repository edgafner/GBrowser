package com.github.gib.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser


class GCustomizeActionGroup(private val jbCefBrowser: JBCefBrowser) :
    ActionGroup("bababa", "Customize and Control", AllIcons.Actions.More), DumbAware {

    private var actions: Collection<AnAction> = emptyList()

    init {
        isPopup = true
        val zoomInButton = GZoomInAction(jbCefBrowser, AllIcons.General.ZoomIn)
        val zoomOutButton = GZoomOutAction(jbCefBrowser, AllIcons.General.ZoomOut)
//        val findButton = GFindAction(jbCefBrowser, AllIcons.Actions.Find)
        actions += zoomInButton
        actions += zoomOutButton
//        actions += findButton
    }


    override fun getChildren(e: AnActionEvent?): Array<AnAction> {

        return actions.toTypedArray()
    }


}

