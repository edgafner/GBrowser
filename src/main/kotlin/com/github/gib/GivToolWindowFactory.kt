package com.github.gib

import com.github.gib.services.GivServiceSettings
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class GivToolWindowFactory : ToolWindowFactory , DumbAware {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        setupToolWindowContent(project,toolWindow)
    }


    private fun setupToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val rootComponent = GivMainPanel(project,GivServiceSettings.instance().getLastSaveHomePage())
        val content = contentManager.factory.createContent(rootComponent, null, false)
        contentManager.addContent(content)
    }

}
