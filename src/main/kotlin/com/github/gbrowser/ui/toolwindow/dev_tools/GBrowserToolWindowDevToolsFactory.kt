package com.github.gbrowser.ui.toolwindow.dev_tools

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.ui.gcef.GBrowserCefDevToolsListener
import com.github.gbrowser.ui.gcef.GCefBrowser
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowContentUiType
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import javax.swing.JComponent

class GBrowserToolWindowDevToolsFactory : ToolWindowFactory, DumbAware {


    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        configureToolWindow(toolWindow)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID) ?: return false
        return toolWindow.contentManager.contents.isNotEmpty()
    }

    private fun configureToolWindow(toolWindow: ToolWindow) {
        ApplicationManager.getApplication().invokeLater {
            toolWindow.setIcon(GBrowserIcons.DEV_TOOLS_ACTIVE)
        }
        toolWindow.setContentUiType(ToolWindowContentUiType.TABBED, null)
        toolWindow.component.apply {
            putClientProperty("HideIdLabel", false)
            putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, true)
        }
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun contentRemoved(event: ContentManagerEvent) {
                if (toolWindow.contentManager.contents.isEmpty() && !toolWindow.isDisposed) {
                    toolWindow.isAvailable = false
                }
            }

            override fun contentAdded(event: ContentManagerEvent) {
                toolWindow.isAvailable = true
            }
        })
    }

    object Companion {

        fun createTab(project: Project, browser: GCefBrowser, tabName: String = "New Tab") {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(GBrowserUtil.DEVTOOLS_TOOL_WINDOW_ID) ?: return

            val contentManager = toolWindow.contentManager
            val existingContent = contentManager.contents.find {
                (it.component as? GBrowserToolWindowDevTools)?.browser?.id == browser.id
            }

            if (existingContent == null) {
                createContent(toolWindow, browser, tabName)
            } else {
                showContent(toolWindow, existingContent)
            }
        }

        private fun createContent(toolWindow: ToolWindow, browser: GCefBrowser, tabName: String) {
            val toolWindowPanel = GBrowserToolWindowDevTools(toolWindow, browser)
            val tabTitle = tabName.take(17)
            val content = toolWindow.contentManager.factory.createContent(toolWindowPanel as JComponent, tabTitle, true)

            content.preferredFocusableComponent = toolWindowPanel.browser.component
            toolWindow.contentManager.addContent(content)
            toolWindow.contentManager.requestFocus(content, true)

            browser.addDevToolsListener(object : GBrowserCefDevToolsListener {
                override fun onDisposeDevtools() {
                    toolWindow.contentManager.removeContent(content, true)
                }
            })

            toolWindow.show()
            showContent(toolWindow, content)
        }

        private fun showContent(toolWindow: ToolWindow, content: Content) {
            toolWindow.contentManager.setSelectedContent(content, true, true)
            toolWindow.show()
        }
    }
}

