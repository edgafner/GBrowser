package com.github.gbrowser.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.CoroutineScope


@Suppress("unused")
@Service(Service.Level.PROJECT)
class GBrowserOpenService(project: Project, private val scope: CoroutineScope) {

  private val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("GBrowser")!!

  @Suppress("UNUSED_PARAMETER")
  fun openGBrowserOnFile(virtualFile: VirtualFile) {

    toolWindow.show()
  }
}