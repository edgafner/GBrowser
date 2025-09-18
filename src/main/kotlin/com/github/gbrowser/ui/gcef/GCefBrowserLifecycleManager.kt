package com.github.gbrowser.ui.gcef

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowBrowser
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the lifecycle of CEF browsers to ensure proper cleanup on project close and IDE exit.
 * This helps prevent the IDE from freezing when exiting after using GBrowser.
 */
@Service(Service.Level.APP)
class GCefBrowserLifecycleManager(private val scope: CoroutineScope) : Disposable {

  companion object {
    private val LOG = thisLogger()

    @JvmStatic
    fun getInstance(): GCefBrowserLifecycleManager {
      return ApplicationManager.getApplication().getService(GCefBrowserLifecycleManager::class.java)
    }
  }

  private val activeBrowsers = ConcurrentHashMap<String, GCefBrowser>()

  init {
    // Register project close listener
    val connection = ApplicationManager.getApplication().messageBus.connect(this)
    connection.subscribe(ProjectManager.TOPIC, object : ProjectManagerListener {
      override fun projectClosing(project: Project) {
        LOG.info("Project closing: ${project.name}, cleaning up GBrowser instances")
        cleanupProjectBrowsers(project)
      }
    })
  }

  fun registerBrowser(browser: GCefBrowser) {
    LOG.info("Registering browser: ${browser.id}")
    activeBrowsers[browser.id] = browser

    // Ensure browser is disposed when its parent is disposed
    Disposer.register(browser) {
      unregisterBrowser(browser.id)
    }
  }

  fun unregisterBrowser(browserId: String) {
    LOG.info("Unregistering browser: $browserId")
    activeBrowsers.remove(browserId)
  }

  fun cleanupProjectBrowsers(project: Project) {
    scope.launch {
      try {
        withContext(Dispatchers.EDT) {
          if (project.isDisposed) {
            LOG.info("Project is already disposed, skipping browser cleanup")
            return@withContext
          }

          try {
            val toolWindowManager = ToolWindowManager.getInstance(project)

            // Clean up main browser window
            toolWindowManager.getToolWindow(GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)?.let { toolWindow ->
              val contentManager = toolWindow.contentManager
              val contents = contentManager.contents

              contents.forEach { content ->
                val component = content.component
                if (component is GBrowserToolWindowBrowser) {
                  disposeBrowserSafely(component.getBrowser())
                }
              }
            }

            // DevTools tool window removed - no longer available in new API (253 EAP)
          } catch (e: CancellationException) {
            LOG.info("Container was disposed during cleanup, this is expected during project close")
            throw e
          } catch (e: Exception) {
            LOG.warn("Non-critical error during browser cleanup", e)
          }
        }
      } catch (e: Exception) {
        LOG.error("Error cleaning up project browsers", e)
      }
    }
  }

  fun cleanupAllBrowsers() {
    LOG.info("Cleaning up all ${activeBrowsers.size} active browsers")

    activeBrowsers.values.forEach { browser ->
      try {
        disposeBrowserSafely(browser)
      } catch (e: Exception) {
        LOG.error("Error disposing browser ${browser.id}", e)
      }
    }

    activeBrowsers.clear()
  }

  private fun disposeBrowserSafely(browser: GCefBrowser) {
    try {
      @Suppress("DEPRECATION")
      if (!Disposer.isDisposed(browser)) {
        LOG.info("Disposing browser: ${browser.id}")

        // Dispose the browser wrapper (which will handle cleanup in its dispose method)
        Disposer.dispose(browser)
      }
    } catch (e: Exception) {
      LOG.error("Error during safe browser disposal", e)
    }
  }

  override fun dispose() {
    LOG.info("GCefBrowserLifecycleManager disposed")
    cleanupAllBrowsers()
    scope.cancel("Service disposed")
  }
}