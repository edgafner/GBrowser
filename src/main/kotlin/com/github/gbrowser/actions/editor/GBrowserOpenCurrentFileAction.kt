package com.github.gbrowser.actions.editor

import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class GBrowserOpenCurrentFileAction : AnAction(), DumbAware {

  companion object {
    private val LOG = thisLogger()
  }

  private val supportedExtensions = setOf("html", "htm", "xhtml", "xml", "svg", "md", "markdown")

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    val file = getCurrentFile(e)

    e.presentation.isEnabledAndVisible = e.project != null && file != null && isSupported(file)
    e.presentation.text = GBrowserBundle.message("action.GBrowserOpenCurrentFileAction.text")
    e.presentation.description = GBrowserBundle.message("action.GBrowserOpenCurrentFileAction.description")
    e.presentation.icon = com.github.gbrowser.GBrowserIcons.GBROWSER_LOGO
  }

  override fun actionPerformed(e: AnActionEvent) {
    if (e.project == null) return
    val file = getCurrentFile(e) ?: return

    if (!isSupported(file)) {
      LOG.info("File type not supported for browser preview: ${file.extension}")
      return
    }

    val url = getFileUrl(file)
    LOG.info("Opening file in GBrowser: $url")

    GBrowserToolWindowUtil.createContentTabAndShow(e, GBrowserUtil.GBROWSER_TOOL_WINDOW_ID, url)
  }

  private fun getCurrentFile(e: AnActionEvent): VirtualFile? {
    // Try to get file from the editor
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor != null) {
      // Get the virtual file from the editor's document
      if (e.project == null) return null
      val document = editor.document
      val fileDocumentManager = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
      return fileDocumentManager.getFile(document)
    }

    // Try to get file from selection in project view
    return e.getData(CommonDataKeys.VIRTUAL_FILE)
  }

  private fun isSupported(file: VirtualFile): Boolean {
    if (file.isDirectory) return false

    val extension = file.extension?.lowercase() ?: return false
    return extension in supportedExtensions
  }

  private fun getFileUrl(file: VirtualFile): String {
    // For local files, use file:// protocol
    val path = file.path
    // Encode the path to handle spaces and special characters
    val encodedPath = path.split(File.separator).joinToString(File.separator) { part ->
      URLEncoder.encode(part, StandardCharsets.UTF_8.toString())
        .replace("+", "%20") // Replace + with %20 for spaces in file URLs
    }

    return if (File.separator == "/") {
      "file://$encodedPath"
    } else {
      // Windows path
      "file:///${encodedPath.replace("\\", "/")}"
    }
  }
}