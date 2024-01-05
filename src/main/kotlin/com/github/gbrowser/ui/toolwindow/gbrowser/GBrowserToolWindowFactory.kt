package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.actions.GBrowserDefaultActionLayouts
import com.github.gbrowser.actions.GBrowserDynamicGroupAction
import com.github.gbrowser.actions.bookmark.GBrowserBookmarkGroupAction
import com.github.gbrowser.settings.GBrowserProjectSetting
import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.VetoableProjectManagerListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.application
import java.util.*


class GBrowserToolWindowFactory : ToolWindowFactory, DumbAware, VetoableProjectManagerListener, ContentManagerListener {

  private val gBrowserSetting = GBrowserSetting.instance()
  private lateinit var gBrowserProjectSetting: GBrowserProjectSetting
  private lateinit var project: Project

  override fun init(toolWindow: ToolWindow) {
    project = toolWindow.project
    gBrowserProjectSetting = project.service<GBrowserProjectSetting>()
    GBrowserDefaultActionLayouts.initialize()


  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    configureToolWindow(toolWindow)
    GBrowserToolWindowBuilder.createContentTab(toolWindow, gBrowserSetting.defaultUrl, "")

    createTabActions(toolWindow)
    createTitleActions(toolWindow)
    createAdditionalGearActions(toolWindow)
  }

  private fun configureToolWindow(toolWindow: ToolWindow) {
    addApplicationListener()
    addSettingsListener(toolWindow)
    application.invokeLater {
      toolWindow.setIcon(GBrowserIcons.GBROWSER_LOGO)
    }
    toolWindow.contentManager.contents.forEach { content ->
      content.putUserData(ToolWindow.SHOW_CONTENT_ICON, gBrowserSetting.isTabIconVisible)
    }

    toolWindow.component.putClientProperty("HideIdLabel", gBrowserSetting.isToolWindowTitleVisible.toString())
    (toolWindow as? ToolWindowEx)?.updateContentUi()

    toolWindow.component.putClientProperty(ToolWindowContentUi.DONT_HIDE_TOOLBAR_IN_HEADER, true)
    toolWindow.component.putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, gBrowserSetting.isDnDEnabled)
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
    gBrowserSetting.addListener { state: GBrowserSetting.SettingsState ->
      toolWindow.component.putClientProperty("HideIdLabel", state.isToolWindowTitleVisible.toString())
      toolWindow.component.putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, state.isDnDEnabled)
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
    GBrowserActionId.GBROWSER_TOGGLE_TOOLBAR_ID.let { id ->
      val action = ActionManager.getInstance().getAction(id)
      toolWindow.setTitleActions(listOf(action))
    }
  }

  private fun createAdditionalGearActions(toolWindow: ToolWindow) {
    val titleActions = DefaultActionGroup()


    val devToolsGroup = GBrowserDynamicGroupAction(GBrowserActionId.DEVTOOLS_GROUP, GBrowserIcons.DEV_TOOLS, "Inspect")
    titleActions.add(devToolsGroup)
    titleActions.add(Separator.create())

    val cleanGroup = GBrowserDynamicGroupAction(GBrowserActionId.CLEAR_BROWSER_DATA, GBrowserIcons.COOKIES, "Clear Cookies and History")
    titleActions.add(cleanGroup)
    titleActions.add(Separator.create())
    titleActions.addAll(GBrowserActionId.BOOKMARK)
    titleActions.add(GBrowserBookmarkGroupAction())
    titleActions.add(Separator.create())
    titleActions.addAll(GBrowserActionId.TABS)
    titleActions.add(Separator.create())
    titleActions.addAll(GBrowserActionId.BROWSER)
    titleActions.add(Separator.create())
    titleActions.addAll(GBrowserActionId.ZOOM)
    titleActions.add(Separator.create())
    titleActions.add(GBrowserActionId.toAction(GBrowserActionId.GBROWSER_PREFERENCES_ID))

    toolWindow.setAdditionalGearActions(titleActions)
  }


  override fun shouldBeAvailable(project: Project) = true

  private fun addApplicationListener() {
    val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
    val topic = AppLifecycleListener.TOPIC
    messageBusConnection.subscribe(topic, object : AppLifecycleListener {
      override fun appClosing() {
        computeHistoryItemsClearing()
        removeSettingsListener()
        removeApplicationListener()
        super.appClosing()
      }
    })
  }


  private fun computeHistoryItemsClearing() {
    val intervalHrs: Int = gBrowserSetting.historyDeleteOption.hours

    if (intervalHrs == 0) {
      gBrowserSetting.removeHistory()
    } else if (intervalHrs != -1) {
      val deleteDate = Date(Date().time - 1000L * 60 * 60 * intervalHrs)
      gBrowserSetting.removeHistory(deleteDate)
      gBrowserProjectSetting.removeTabs(deleteDate)
    }
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

  @Suppress("unused")
  private fun addProjectListener() {
    ProjectManager.getInstance().addProjectManagerListener(this as VetoableProjectManagerListener)
  }

  @Suppress("unused")
  private fun removeProjectListener() {
    ProjectManager.getInstance().removeProjectManagerListener(this as VetoableProjectManagerListener)
  }


  override fun canClose(project: Project): Boolean = true

  //private fun createTabsOnAppStart(toolWindow: ToolWindow) {
  //  val tabs = gbrowserProjectService.tabs
  //  if (tabs.isEmpty()) {
  //    GBrowserToolWindowBuilder.createContentTab(toolWindow = toolWindow, url = null, tabName = null)
  //  } else {
  //    for (tab in tabs) {
  //      GBrowserToolWindowBuilder.createContentTab(toolWindow, tab.url, tab.name)
  //    }
  //    gbrowserProjectService.removeTabs(tabs)
  //  }
  //}


  //private fun storeTabsOnClosingProject(project: Project) {
  //  val toolWindow = getToolWindow(project, "GBrowser")
  //  toolWindow?.let {
  //    val contentManager = it.contentManager
  //    val tabs = contentManager.contents
  //
  //    for (element in tabs) {
  //      val panel = element.component as? GBrowserToolWindowBrowser
  //      val url = panel?.getCurrentUrl() ?: continue
  //      val title = panel.getCurrentTitle()
  //      gbrowserProjectService.addTab(GBrowserTab(url, title, Date()))
  //    }
  //  }
  //}
}


fun getSelectedBrowserPanel(anActionEvent: AnActionEvent): GBrowserToolWindowBrowser? {
  val project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT)
  return getSelectedBrowserPanel(project)
}

fun getSelectedBrowserPanel(project: Project): GBrowserToolWindowBrowser? {
  val toolWindow = getToolWindow(project, GBrowserUtil.GROUP_DISPLAY_ID) ?: return null
  val selectedContent = toolWindow.contentManager.selectedContent ?: return null
  return selectedContent.component as? GBrowserToolWindowBrowser
}


