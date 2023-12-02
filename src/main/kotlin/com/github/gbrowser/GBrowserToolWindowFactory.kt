package com.github.gbrowser

import com.github.gbrowser.ui.toolwindow.GBrowserToolWindowTabComponentFactory
import com.github.gbrowser.ui.toolwindow.base.dontHideOnEmptyContent
import com.github.gbrowser.ui.toolwindow.base.manageBrowserToolwindowTabs
import com.github.gbrowser.ui.toolwindow.model.GBrowserToolWindowViewModel
import com.github.gbrowser.uitl.cancelOnDispose
import com.github.gbrowser.uitl.childScope
import com.github.gbrowser.uitl.serviceAsync
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.util.concurrency.annotations.RequiresEdt
import kotlinx.coroutines.*

class GBrowserToolWindowFactory : ToolWindowFactory, DumbAware {

  override suspend fun manage(toolWindow: ToolWindow, toolWindowManager: ToolWindowManager) {
    toolWindow.project.serviceAsync<GBrowserToolWindowController>().manageAvailability(toolWindow)
  }

  override fun createToolWindowContent(project: Project,
                                       toolWindow: ToolWindow) = project.service<GBrowserToolWindowController>().manageContent(toolWindow)

  override fun shouldBeAvailable(project: Project): Boolean = false

  @Service(Service.Level.PROJECT)
  private class GBrowserToolWindowController(private val project: Project, parentCs: CoroutineScope) {
    private val cs = parentCs.childScope(Dispatchers.Main)

    suspend fun manageAvailability(toolWindow: ToolWindow) {
      coroutineScope {
        val vm = project.serviceAsync<GBrowserToolWindowViewModel>()
        launch {
          vm.isAvailable.collect {
            withContext(Dispatchers.EDT) {
              toolWindow.isAvailable = it
            }
          }
        }

        launch {
          vm.activationRequests.collect {
            withContext(Dispatchers.EDT) {
              toolWindow.activate(null)
            }
          }
        }
      }
    }

    @RequiresEdt
    fun manageContent(toolWindow: ToolWindow) {
      toolWindow.component.putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true")
      toolWindow.dontHideOnEmptyContent()

      cs.launch {
        val vm = project.serviceAsync<GBrowserToolWindowViewModel>()
        val componentFactory = GBrowserToolWindowTabComponentFactory(project)

        manageBrowserToolwindowTabs(this, toolWindow, vm, componentFactory, "GBrowser")
        val action = ActionManager.getInstance().getAction("GBrowser.NewTab")
        val wrapper = if (action is ActionGroup) ActionGroupWrapper(action) else AnActionWrapper(action)
        wrapper.registerCustomShortcutSet(CommonShortcuts.getNew(), toolWindow.component)
        toolWindow.setTitleActions(listOf(wrapper))
        awaitCancellation()
      }.cancelOnDispose(toolWindow.contentManager)
    }
  }

}
