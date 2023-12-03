package com.github.gbrowser

import com.github.gbrowser.ui.toolwindow.GBrowserToolWindowTabComponentFactory
import com.github.gbrowser.ui.toolwindow.base.dontHideOnEmptyContent
import com.github.gbrowser.ui.toolwindow.base.manageBrowserToolwindowTabs
import com.github.gbrowser.ui.toolwindow.model.GBrowserToolWindowViewModel
import com.github.gbrowser.util.cancelOnDispose
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.util.childScope
import com.intellij.util.concurrency.annotations.RequiresEdt
import kotlinx.coroutines.*

internal class GBrowserToolWindowFactory(private val scope: CoroutineScope) : ToolWindowFactory, DumbAware {

  override fun init(toolWindow: ToolWindow) {
    scope.launch {
      toolWindow.project.service<GBrowserToolWindowController>().manageAvailability(toolWindow)
    }
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    runInEdt {
      project.service<GBrowserToolWindowController>().manageContent(toolWindow)
    }
  }

  override fun shouldBeAvailable(project: Project): Boolean = false

}

@Suppress("UnstableApiUsage")
@Service(Service.Level.PROJECT)
internal class GBrowserToolWindowController(private val project: Project, parentCs: CoroutineScope) {
  private val cs = parentCs.childScope(Dispatchers.Main)

  suspend fun manageAvailability(toolWindow: ToolWindow) {
    val vm = project.service<GBrowserToolWindowViewModel>()
    coroutineScope {
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
      val vm = project.service<GBrowserToolWindowViewModel>()
      val componentFactory = GBrowserToolWindowTabComponentFactory()

      manageBrowserToolwindowTabs(this, toolWindow, vm, componentFactory, "GBrowser")
      val action = ActionManager.getInstance().getAction("GBrowser.NewTab")
      val wrapper = if (action is ActionGroup) ActionGroupWrapper(action) else AnActionWrapper(action)
      wrapper.registerCustomShortcutSet(CommonShortcuts.getNew(), toolWindow.component)
      toolWindow.setTitleActions(listOf(wrapper))
      awaitCancellation()
    }.cancelOnDispose(toolWindow.contentManager)
  }
}

