package com.github.gbrowser.ui.toolwindow.base

import com.github.gbrowser.ui.toolwindow.model.GBrowserViewModel
import com.github.gbrowser.util.cancelledWith
import com.intellij.collaboration.async.launchNow
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.*
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.childScope
import com.intellij.util.namedChildScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.annotations.Nls

/**
 * Manages gbrowser toolwindow tabs and their content.
 * @see GBrowserToolwindowDataKeys
 */
fun <T : GBrowserTab, TVM : GBrowserTabViewModel, PVM : GBrowserToolwindowProjectViewModel<T, TVM>> manageBrowserToolwindowTabs(cs: CoroutineScope,
                                                                                                                                toolwindow: ToolWindow,
                                                                                                                                gbrowserToolwindowViewModel: GBrowserToolwindowViewModel<PVM>,
                                                                                                                                tabComponentFactory: GBrowserTabsComponentFactory<TVM, PVM>,
                                                                                                                                tabTitle: @Nls String) {
  GBrowserToolwindowTabsManager(cs, toolwindow, gbrowserToolwindowViewModel, tabComponentFactory, tabTitle)
}

@Suppress("UnstableApiUsage")
private class GBrowserToolwindowTabsManager<T : GBrowserTab, TVM : GBrowserTabViewModel, PVM : GBrowserToolwindowProjectViewModel<T, TVM>>(
  parentCs: CoroutineScope,
  private val toolwindow: ToolWindow,
  private val gbrowserToolwindowViewModel: GBrowserToolwindowViewModel<PVM>,
  private val tabComponentFactory: GBrowserTabsComponentFactory<TVM, PVM>,
  private val tabTitle: @Nls String) {


  private val contentManager = toolwindow.contentManager
  private val projectVm = gbrowserToolwindowViewModel.projectVm
  private val cs = parentCs.childScope(Dispatchers.Main)

  init {
    contentManager.addDataProvider {
      when {
        GBrowserToolwindowDataKeys.GBROWSER_TOOLWINDOW_PROJECT_VM.`is`(it) -> projectVm.value
        GBrowserToolwindowDataKeys.GBROWSER_TOOLWINDOW_VM.`is`(it) -> gbrowserToolwindowViewModel
        else -> null
      }
    }

    @Suppress("UnstableApiUsage") cs.launchNow {
      projectVm.collectLatest { vm ->
        try {
          manageProjectTabs(vm!!)
        }
        catch (e: Exception) {
          withContext(NonCancellable) {
            contentManager.removeAllContents(true)
          }
        }
      }
    }
  }

  private suspend fun manageProjectTabs(projectVm: PVM) {
    val mainContent = createMainModelContent(projectVm)
    withContext(NonCancellable) {
      contentManager.addContent(mainContent)
      contentManager.setSelectedContent(mainContent)
    }
    refreshTabOnTabSelection(projectVm.browserVm, contentManager, mainContent)
    refreshOnToolwindowShow(projectVm.browserVm, toolwindow, mainContent)

    currentCoroutineContext().ensureActive()

    // required for backwards sync contentManager -> VM
    val syncListener = object : ContentManagerListener {
      override fun contentRemoved(event: ContentManagerEvent) {
        event.content.getUserData(GBROWSER_TAB_KEY)?.let {
          projectVm.closeTab(it)
        }
      }

      override fun selectionChanged(event: ContentManagerEvent) {
        if (event.operation == ContentManagerEvent.ContentOperation.add) {
          event.content.getUserData(GBROWSER_TAB_KEY).let {
            projectVm.selectTab(it)
          }
        }
      }
    }

    projectVm.tabs.collect { tabsState ->
      contentManager.removeContentManagerListener(syncListener)
      contentManager.contents.forEach { content ->
        if (content !== mainContent) {
          val tab = content.getUserData(GBROWSER_TAB_KEY)
          if (tab == null || !tabsState.tabs.containsKey(tab)) {
            contentManager.removeContent(content, true)
          }
        }
      }

      for ((tabType, tabVm) in tabsState.tabs) {
        val existing = findTabContent(tabType)
        if (existing == null || existing.getUserData(GBROWSER_TAB_VM_KEY) !== tabVm) {
          closeExistingTabAndCreateNew(tabType, projectVm, tabVm)
        }
      }

      val contentToSelect = tabsState.selectedTab?.let(::findTabContent) ?: mainContent
      contentManager.setSelectedContent(contentToSelect, true)
      contentManager.addContentManagerListener(syncListener)
    }
  }

  private fun findTabContent(gBrowserTab: T): Content? = contentManager.contents.find { it.getUserData(GBROWSER_TAB_KEY) == gBrowserTab }

  private fun closeExistingTabAndCreateNew(tab: T, projectVm: PVM, tabVm: TVM) {
    val existingContent = findTabContent(tab)
    if (existingContent != null) {
      contentManager.removeContent(existingContent, true)
    }

    val content = createTabContent(tab, projectVm, tabVm)
    contentManager.addContent(content)
  }

  private fun createMainModelContent(projectVm: PVM): Content = createDisposableContent(
    createTabDebugName(projectVm.projectName)) { content, contentCs ->
    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, java.lang.Boolean.TRUE)
    content.isCloseable = false

    content.component = tabComponentFactory.createGBrowserComponent(contentCs, projectVm)
  }

  private fun createTabContent(tab: T, projectVm: PVM, tabVm: TVM): Content = createDisposableContent(
    createTabDebugName(tabVm.displayName)) { content, contentCs ->
    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, java.lang.Boolean.TRUE)
    content.isCloseable = true
    content.icon = tabVm.icon
    content.displayName = ""
    content.description = ""

    content.component = tabComponentFactory.createTabComponent(contentCs, projectVm, tabVm, content::setIcon)

    content.putUserData(GBROWSER_TAB_KEY, tab)
    content.putUserData(GBROWSER_TAB_VM_KEY, tabVm)
  }

  @Suppress("UnstableApiUsage")
  private fun createDisposableContent(debugName: String, modifier: (Content, CoroutineScope) -> Unit): Content {
    val factory = ContentFactory.getInstance()
    return factory.createContent(null, tabTitle, false).apply {
      val disposable = Disposer.newDisposable()
      setDisposer(disposable)
      modifier(this, cs.namedChildScope(debugName).cancelledWith(disposable))
    }
  }

  private fun createTabDebugName(name: String) = "GBrowser Toolwindow Tab [$name]"

  @Suppress("PrivatePropertyName")
  private val GBROWSER_TAB_KEY: Key<T> = Key.create("com.github.gbrowser.ui.toolwindow.base.tab")

  @Suppress("PrivatePropertyName")
  private val GBROWSER_TAB_VM_KEY: Key<TVM> = Key.create("com.github.gbrowser.ui.toolwindow.base.tab.vm")
}


private fun refreshTabOnTabSelection(listVm: GBrowserViewModel, contentManager: ContentManager, content: Content) {
  val listener = object : ContentManagerListener {
    override fun selectionChanged(event: ContentManagerEvent) {
      if (event.operation == ContentManagerEvent.ContentOperation.add && event.content === content) { // tab selected
        listVm.refresh()
      }
    }
  }
  contentManager.addContentManagerListener(listener)
  Disposer.register(content) {
    contentManager.removeContentManagerListener(listener)
  }
}

private fun refreshOnToolwindowShow(listVm: GBrowserViewModel, toolwindow: ToolWindow, content: Content) {
  toolwindow.project.messageBus.connect(content).subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
    override fun toolWindowShown(shownToolwindow: ToolWindow) {
      if (shownToolwindow.id == toolwindow.id) {
        val selectedContent = shownToolwindow.contentManager.selectedContent
        if (selectedContent === content) {
          listVm.refresh()
        }
      }
    }
  })
}
