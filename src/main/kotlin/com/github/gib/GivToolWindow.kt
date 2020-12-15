package com.github.gib

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

class GivToolWindow : DumbAware, Disposable {

    private val givPanel: GivMainPanel = GivMainPanel()

    fun initializePanel(toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()

        val content: Content = contentFactory.createContent(null, null, false)
        content.component = givPanel

        Disposer.register(this, givPanel)
        toolWindow.contentManager.addContent(content)
    }

    companion object {
        fun getInstance(project: Project): GivToolWindow = ServiceManager.getService(project, GivToolWindow::class.java)
    }

    override fun dispose() {}
}
