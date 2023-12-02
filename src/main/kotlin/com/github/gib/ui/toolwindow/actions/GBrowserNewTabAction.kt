package com.github.gib.ui.toolwindow.actions

import com.github.gib.GBrowserBundle
import com.github.gib.ui.toolwindow.model.GBrowserToolWindowViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import java.util.*

open class GBrowserNewTabAction : DumbAwareAction("New Tab",
                                                  "Create new session in new tab",
                                                  AllIcons.General.Add) {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    with(e) {
      val vm = project?.service<GBrowserToolWindowViewModel>()
      val twAvailable = project != null && vm != null && vm.isAvailable.value
      val twInitialized = project != null && vm != null && vm.projectVm.value != null

      if (place == ActionPlaces.TOOLWINDOW_TITLE) {
        presentation.isEnabledAndVisible = twInitialized
        presentation.icon = AllIcons.General.Add
      }
      else {
        presentation.isEnabledAndVisible = twAvailable
        presentation.icon = AllIcons.Vcs.Vendors.Github
      }
    }
  }

  override fun actionPerformed(e: AnActionEvent) = tryToCreatePullRequest(e)

}


private fun tryToCreatePullRequest(e: AnActionEvent) {
  return e.getRequiredData(PlatformDataKeys.PROJECT).service<GBrowserToolWindowViewModel>().activateAndAwaitProject {
    createNewGBrowserTab(UUID.randomUUID().toString())
  }
}