package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.services.GBrowserProjectService
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectCloseListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.application
import java.util.*


@Suppress("UnstableApiUsage")
class GBrowserToolWindowFactory : ToolWindowFactory, DumbAware, ContentManagerListener {

  private lateinit var myGBrowserProjectService: GBrowserProjectService
  private lateinit var project: Project
  private lateinit var myGBrowserService: GBrowserService

  override fun init(toolWindow: ToolWindow) {
    project = toolWindow.project
    myGBrowserService = project.service<GBrowserService>()
    myGBrowserProjectService = project.service<GBrowserProjectService>()
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    configureToolWindow(toolWindow)
    if (!myGBrowserService.reloadTabOnStartup || myGBrowserProjectService.tabs.isEmpty()) {
      GBrowserToolWindowUtil.createContentTab(toolWindow, myGBrowserService.defaultUrl, "")
    }
    createTitleActions(toolWindow)
    createAdditionalGearActions(toolWindow)

    // Register listener only for tab restoration - visibility handling is done in configureToolWindow
    project.messageBus.connect(toolWindow.disposable).subscribe<ToolWindowManagerListener>(
      ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
        override fun toolWindowsRegistered(ids: MutableList<String>, toolWindowManager: ToolWindowManager) {
          if (ids.contains(
              GBrowserUtil.GBROWSER_TOOL_WINDOW_ID
            ) && myGBrowserService.reloadTabOnStartup) {
            createTabsOnAppStart(toolWindow)
          }
        }
      })

    project.messageBus.connect(toolWindow.disposable).subscribe(ProjectCloseListener.TOPIC, object : ProjectCloseListener {
        override fun projectClosingBeforeSave(project: Project) {
          toolWindow.let {
            val contentManager = it.contentManager
            val tabs = contentManager.contents

            val gBrowserTabs = tabs.take(5).mapNotNull { con ->
              val panel = con.component as? GBrowserToolWindowBrowser
              panel?.getCurrentUrl()?.let { currentUrl ->
                val title = panel.getCurrentTitle()
                GBrowserTab(currentUrl, title, Date())
              }
            }
            myGBrowserProjectService.addTabs(gBrowserTabs)
          }
        }
      })

    // We'll handle visibility management through ToolWindowManagerListener instead
  }

  private fun configureToolWindow(toolWindow: ToolWindow) {
    addApplicationListener()
    addSettingsListener(toolWindow)
    application.invokeLaterOnWriteThread {
      (toolWindow as? ToolWindowEx)?.updateContentUi()
      toolWindow.setIcon(GBrowserIcons.GBROWSER_LOGO)
    }
    toolWindow.contentManager.contents.forEach { content ->
      content.putUserData(ToolWindow.SHOW_CONTENT_ICON, myGBrowserService.isTabIconVisible)
    }

    toolWindow.component.putClientProperty("HideIdLabel", myGBrowserService.hideIdLabel.toString())
    (toolWindow as? ToolWindowEx)?.updateContentUi()

    toolWindow.component.putClientProperty(ToolWindowContentUi.DONT_HIDE_TOOLBAR_IN_HEADER, true)
    toolWindow.component.putClientProperty(
      ToolWindowContentUi.ALLOW_DND_FOR_TABS, myGBrowserService.isDragAndDropEnabled
    )

    project.messageBus.connect(toolWindow.disposable).subscribe(
      ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
        private var wasVisible = false
        private var lastPinState = toolWindow.isAutoHide // isAutoHide is true for unpinned mode

        override fun stateChanged(toolWindowManager: ToolWindowManager) {
          val gbrowserToolWindow = toolWindowManager.getToolWindow(GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)
          if (gbrowserToolWindow != null) {
            val isVisible = gbrowserToolWindow.isVisible
            val isUnpinned = gbrowserToolWindow.isAutoHide // unpinned mode

            // Detect visibility changes (becoming visible) or pin state changes
            if ((isVisible && !wasVisible) || (isUnpinned != lastPinState)) { // When becoming visible again or when pin state changes,

              // First immediate refresh
              ApplicationManager.getApplication().invokeLater {
                refreshBrowserVisibility(gbrowserToolWindow)
              }

              // Execute multiple refresh attempts with increasing delays
              // This creates a more robust approach
              // to ensure visibility in unpinned mode
              ApplicationManager.getApplication().executeOnPooledThread {
                val delaySequence = listOf(200L, 500L, 800L) // Progressive delays
                for (delay in delaySequence) {
                  try {
                    Thread.sleep(delay)
                    ApplicationManager.getApplication().invokeLater {
                      if (gbrowserToolWindow.isVisible) { // Only if still visible
                        refreshBrowserVisibility(gbrowserToolWindow)
                      }
                    }
                  } catch (e: InterruptedException) { // Handle thread interruption
                    thisLogger().warn("GBrowserToolWindowFactory: Interrupted refresh thread", e)
                    Thread.currentThread().interrupt()
                    break
                  }
                }
              }
            }

            // Update state tracking
            wasVisible = isVisible
            lastPinState = isUnpinned
          }
        }

        override fun toolWindowsRegistered(ids: MutableList<String>, toolWindowManager: ToolWindowManager) { // Handle this case in the other listener
        }

        private fun refreshBrowserVisibility(toolWindow: ToolWindow) {
          toolWindow.contentManager.contents.forEach { content -> // Force visibility for all tabs, not just selected ones
            val component = content.component as? GBrowserToolWindowBrowser
            component?.let { // Force browser visibility
              it.getBrowser().setVisibility(true)

              // Force complete UI refresh
              it.invalidate()
              it.validate()
              it.repaint()

              // Also ensure the component's parent is visible and refreshed
              val parent = it.parent
              parent?.invalidate()
              parent?.validate()
              parent?.repaint()
            }
          }
        }
      })
    toolWindow.contentManager.addContentManagerListener((object : ContentManagerListener {
      override fun selectionChanged(event: ContentManagerEvent) {
        if (event.content.isSelected) {
          val component = event.content.component
          val panel = component as? GBrowserToolWindowBrowser
          if (panel != null) { // Don't automatically request focus - let the user maintain control
            // Schedule with a small delay to ensure the UI is ready
            ApplicationManager.getApplication().invokeLater {
              val browser = panel.getBrowser() // Force visibility and ensure browser is properly displayed
              browser.setVisibility(true)

              // Force component layout update
              panel.invalidate() // This is important for unpinned mode
              panel.validate()
              panel.repaint()

              // Schedule another refresh after a short delay for unpinned mode
              ApplicationManager.getApplication().executeOnPooledThread {
                Thread.sleep(200) // Short delay
                ApplicationManager.getApplication().invokeLater {
                  if (event.content.isSelected) { // Double-check it's still selected
                    browser.setVisibility(true)
                    panel.validate()
                    panel.repaint()
                  }
                }
              }
            }
          }
        }
      }
    } as ContentManagerListener))
    (toolWindow as? ToolWindowEx)?.updateContentUi()
  }


  private fun addSettingsListener(toolWindow: ToolWindow) {
    myGBrowserService.addListener { state: GBrowserService.SettingsState ->
      toolWindow.component.putClientProperty("HideIdLabel", state.hideIdLabel.toString())
      toolWindow.component.putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, state.isDragAndDropEnabled)
      toolWindow.contentManager.contents.forEach { content ->
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, state.isTabIconVisible)
      }
    }
  }


  @Suppress("unused")
  private fun createTabActions(toolWindow: ToolWindow) {
    GBrowserActionId.GBROWSER_ADD_TAB_ID.let { id ->
      val action = ActionManager.getInstance().getAction(id)
      (toolWindow as ToolWindowEx).setTabActions(action)
    }
  }

  private fun createTitleActions(toolWindow: ToolWindow) {
    toolWindow.setTitleActions(GBrowserActionId.titleActions())
  }

  private fun createAdditionalGearActions(toolWindow: ToolWindow) {
    val actionGroup = DefaultActionGroup()
    actionGroup.addAll(GBrowserActionId.allActions())
    toolWindow.setAdditionalGearActions(actionGroup)
  }


  override fun shouldBeAvailable(project: Project) = true

  private fun addApplicationListener() {
    val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
    val topic = AppLifecycleListener.TOPIC
    messageBusConnection.subscribe(topic, object : AppLifecycleListener {
      override fun appClosing() { // Dispose of all GBrowserToolWindowBrowser instances
        ToolWindowManager.getInstance(project).getToolWindow(GBrowserUtil.GBROWSER_TOOL_WINDOW_ID)?.let { toolWindow ->
          toolWindow.contentManager.contents.forEach { content ->
            (content.component as? GBrowserToolWindowBrowser)?.dispose()
          }
        }

        // Remove listeners and perform additional cleanup
        removeSettingsListener()
        removeApplicationListener()

        super.appClosing()
      }
    })
  }

  private fun removeSettingsListener() {
    val messageBus = ApplicationManager.getApplication().messageBus
    if (!messageBus.isDisposed) {
      ApplicationManager.getApplication().messageBus.dispose()
    }
  }

  private fun removeApplicationListener() {
    val messageBus = ApplicationManager.getApplication().messageBus
    if (!messageBus.isDisposed) {
      ApplicationManager.getApplication().messageBus.dispose()
    }
  }

  private fun createTabsOnAppStart(toolWindow: ToolWindow) {
    val tabs = myGBrowserProjectService.tabs
    if (tabs.isNotEmpty()) {
      for (tab in tabs) {
        GBrowserToolWindowUtil.createContentTab(toolWindow, tab.url, tab.name)
      }
    }
  }


}




