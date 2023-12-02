package com.github.gib.ui.toolwindow.listeners

import com.intellij.execution.ui.BaseContentCloseListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.content.Content

class GBrowserTabCloseListener(val content: Content,
                               val project: Project,
                               parentDisposable: Disposable) : BaseContentCloseListener(content, project, parentDisposable) {
  override fun disposeContent(content: Content) {
  }

  override fun closeQuery(content: Content, projectClosing: Boolean): Boolean {
    return true
  }

  override fun canClose(project: Project): Boolean {
    return project === this.project && closeQuery(this.content, true)
  }

  companion object {
    fun executeContentOperationSilently(content: Content, runnable: () -> Unit) {
      content.putUserData(SILENT, true)
      try {
        runnable()
      }
      finally {
        content.putUserData(SILENT, null)
      }
    }
  }
}

private val SILENT = Key.create<Boolean>("Silent content operation")