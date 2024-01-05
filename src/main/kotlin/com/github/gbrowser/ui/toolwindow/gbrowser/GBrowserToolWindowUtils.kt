package com.github.gbrowser.ui.toolwindow.gbrowser

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentManager


fun getToolWindowManager(project: Project?): ToolWindowManager? {
  return project?.let { ToolWindowManager.getInstance(it) }
}

fun getToolWindow(project: Project?, id: String): ToolWindow? {
  val toolWindowManager = getToolWindowManager(project)
  return toolWindowManager?.getToolWindow(id)
}

fun getContentManager(project: Project?, id: String): ContentManager? {
  val toolWindowManager = getToolWindowManager(project)
  return toolWindowManager?.getToolWindow(id)?.contentManager
}



