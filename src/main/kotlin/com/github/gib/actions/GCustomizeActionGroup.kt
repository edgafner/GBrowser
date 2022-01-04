package com.github.gib.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser


class GCustomizeActionGroup(jbCefBrowser: JBCefBrowser) :
    ActionGroup("Customize and Control", "Customize and Control", AllIcons.Actions.More), DumbAware {

    private var actions: Collection<AnAction> = emptyList()

    init {
        isPopup = true
        isSearchable = false
        templatePresentation.putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true)

        val zoomInButton = GZoomInAction(jbCefBrowser, AllIcons.General.ZoomIn)
        val zoomOutButton = GZoomOutAction(jbCefBrowser, AllIcons.General.ZoomOut)
        val findButton = GFindAction(jbCefBrowser, AllIcons.Actions.Find)

//        val cookieIcon = IconLoader.getIcon("/icons/cookie.png", javaClass)

//        val cookiesButton = GCookiesAction(jbCefBrowser, AllIcons.Nodes.EmptyNode, givMainPanel)

        findButton.registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts(IdeActions.ACTION_FIND),
            jbCefBrowser.component)


        actions += zoomInButton
        actions += zoomOutButton
        actions += findButton
        actions += Separator()
        // Maybe we should disable this for now
//        actions += cookiesButton
    }


    override fun getChildren(e: AnActionEvent?): Array<AnAction> {

        return actions.toTypedArray()
    }


}

