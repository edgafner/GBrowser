package com.github.gbrowser.actions

import com.github.gbrowser.services.GBrowserOpenService
import com.intellij.ide.actions.RevealFileAction
import com.intellij.ide.lightEdit.LightEdit
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile


@Suppress("ComponentNotRegistered")
class OpenFileInGBrowserAction : DumbAwareAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    val selectedFile: VirtualFile? = getSelectedFile(e)
    if (project == null || selectedFile == null) {
      return
    }

    e.project?.service<GBrowserOpenService>()?.openGBrowserOnFile(selectedFile)
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = isAvailable(e)
  }

  private fun isAvailable(e: AnActionEvent): Boolean {
    val project = e.project
    val editor = e.getData(CommonDataKeys.EDITOR)
    return project != null && !LightEdit.owns(project) && getSelectedFile(e) != null && (!ActionPlaces.isPopupPlace(
      e.place) || editor == null || !editor.selectionModel.hasSelection())
  }

  private fun getSelectedFile(e: AnActionEvent): VirtualFile? {
    return RevealFileAction.findLocalFile(e.getData(CommonDataKeys.VIRTUAL_FILE))
  }

}


