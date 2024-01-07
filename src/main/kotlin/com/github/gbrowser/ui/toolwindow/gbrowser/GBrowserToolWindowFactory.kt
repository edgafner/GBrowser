package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.settings.GBrowserProjectService
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.*
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

  private val myGBrowserService = GBrowserService.instance()
  private lateinit var myGBrowserProjectService: GBrowserProjectService
  private lateinit var project: Project

  override fun init(toolWindow: ToolWindow) {
    project = toolWindow.project
    myGBrowserProjectService = project.service<GBrowserProjectService>()

  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    configureToolWindow(toolWindow)
    if (!myGBrowserService.reloadTabOnStartup || myGBrowserProjectService.tabs.isEmpty()) {
      GBrowserToolWindowUtil.createContentTab(toolWindow, myGBrowserService.defaultUrl, "")
    }
    //createTabActions(toolWindow)
    createTitleActions(toolWindow)
    createAdditionalGearActions(toolWindow)

    project.messageBus.connect(toolWindow.disposable).subscribe<ToolWindowManagerListener>(ToolWindowManagerListener.TOPIC,
                                                                                           object : ToolWindowManagerListener {
                                                                                             override fun toolWindowsRegistered(ids: MutableList<String>,
                                                                                                                                toolWindowManager: ToolWindowManager) {
                                                                                               if (ids.contains(
                                                                                                   GBrowserUtil.GBROWSER_TOOL_WINDOW_ID) && myGBrowserService.reloadTabOnStartup) {
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

  }

  private fun configureToolWindow(toolWindow: ToolWindow) {
    addApplicationListener()
    addSettingsListener(toolWindow)
    application.invokeLater {
      toolWindow.setIcon(GBrowserIcons.GBROWSER_LOGO)
    }
    toolWindow.contentManager.contents.forEach { content ->
      content.putUserData(ToolWindow.SHOW_CONTENT_ICON, myGBrowserService.isTabIconVisible)
    }

    toolWindow.component.putClientProperty("HideIdLabel", myGBrowserService.hideIdLabel.toString())
    (toolWindow as? ToolWindowEx)?.updateContentUi()

    toolWindow.component.putClientProperty(ToolWindowContentUi.DONT_HIDE_TOOLBAR_IN_HEADER, true)
    toolWindow.component.putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, myGBrowserService.isDragAndDropEnabled)
    toolWindow.contentManager.addContentManagerListener((object : ContentManagerListener {
      override fun selectionChanged(event: ContentManagerEvent) {
        if (event.content.isSelected) {
          val component = event.content.component
          val panel = if (component is GBrowserToolWindowBrowser) component else null
          if (panel != null) {
            val browser = panel.getBrowser()
            val browserComponent = browser.component
            browserComponent.requestFocus()
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
      (toolWindow as? ToolWindowEx)?.updateContentUi()
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
      override fun appClosing() {
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




