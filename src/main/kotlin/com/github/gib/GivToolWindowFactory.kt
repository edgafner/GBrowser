package com.github.gib

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class GivToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val givExplorer = GivToolWindow.getInstance(project)
        givExplorer.initializePanel(toolWindow)
    }
}
