package com.github.gbrowser.actions.toolwindow

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowManager.Companion.getInstance

class GBrowserAddTabAction : AnAction(), DumbAware {

    init {
        isEnabledInModalContext = true

    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        getInstance(project).getToolWindow(GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)?.let {
            GBrowserToolWindowUtil.createContentTab(it, GBrowserService.instance().defaultUrl, "")
        }
    }
}

