package com.github.gib

import com.github.gib.actions.GBrowserActionKeys
import com.github.gib.ui.toolwindow.GBrowserToolWindowTabComponentFactory
import com.github.gib.ui.toolwindow.base.dontHideOnEmptyContent
import com.github.gib.ui.toolwindow.base.manageReviewToolwindowTabs
import com.github.gib.ui.toolwindow.model.GBrowserToolWindowViewModel
import com.github.gib.uitl.cancelOnDispose
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi

import com.github.gib.uitl.childScope
import com.intellij.util.concurrency.annotations.RequiresEdt
import kotlinx.coroutines.*

@Suppress("UnstableApiUsage")
class GBrowserToolWindowFactory : ToolWindowFactory, DumbAware {

  override suspend fun manage(toolWindow: ToolWindow, toolWindowManager: ToolWindowManager) {
    toolWindow.project.serviceAsync<GBrowserToolWindowController>().manageAvailability(toolWindow)
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) =
    project.service<GBrowserToolWindowController>().manageContent(toolWindow)

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

      cs.launch {
        val vm = project.serviceAsync<GBrowserToolWindowViewModel>()

        coroutineScope {
          toolWindow.contentManager.addDataProvider {
            if (GBrowserActionKeys.GBROWSER_PROJECT_VM.`is`(it)) vm.projectVm.value
            else null
          }

          // so it's not closed when all content is removed
          toolWindow.dontHideOnEmptyContent()
          val componentFactory = GBrowserToolWindowTabComponentFactory(project)
          manageReviewToolwindowTabs(this, toolWindow, vm, componentFactory, "GBrowser")
          val wrapper = ActionUtil.wrap("GBrowser.NewTab")
          wrapper.registerCustomShortcutSet(CommonShortcuts.getNew(), toolWindow.component)
          toolWindow.setTitleActions(listOf(wrapper))
          //toolWindow.setAdditionalGearActions(DefaultActionGroup(GHPRSwitchRemoteAction()))

          awaitCancellation()
        }
      }.cancelOnDispose(toolWindow.contentManager)
    }
  }

  companion object {
        @Suppress("unused")
        const val GBROWSER_TOOL_WINDOW_ID = "GBrowser"
      }
}

//  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//    val gBrowserToolWindowManager = GBrowserToolWindowManager.getInstance(project)
//    gBrowserToolWindowManager.initToolWindow(toolWindow as ToolWindowEx)
//    //setupToolWindowContent(toolWindow)
//  }
//
//
//  private fun setupToolWindowContent(toolWindow: ToolWindow) {
//    val contentManager = toolWindow.contentManager
//    val rootComponent = GivMainPanel(GivServiceSettings.instance().getLastSaveHomePage())
//    val content = contentManager.factory.createContent(rootComponent, null, false)
//    contentManager.addContent(content)
//  }
//
//
//  companion object {
//    @Suppress("unused")
//    const val GBROWSER_TOOL_WINDOW_ID = "GBrowser"
//  }
//
//}
