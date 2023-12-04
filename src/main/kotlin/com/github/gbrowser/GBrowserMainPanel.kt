package com.github.gbrowser

import com.github.gbrowser.actions.*
import com.github.gbrowser.gcef.GBCefBrowser
import com.github.gbrowser.gcef.GBRequestHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase.ErrorPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.cef.handler.CefLoadHandler
import javax.swing.Icon


class GBrowserMainPanel(private val initialUrl: String,
                        private val callback: (Icon) -> Unit,
                        private val contentCs: CoroutineScope) : SimpleToolWindowPanel(true, true), Disposable {

  private val jbCefBrowser: JBCefBrowser = GBCefBrowser(initialUrl)


  init {
    val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.CONTEXT_TOOLBAR, buildToolbar(), true).apply {
      targetComponent = this@GBrowserMainPanel
    }

    jbCefBrowser.setErrorPage { errorCode, errorText, failedUrl ->
      if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) null
      else ErrorPage.DEFAULT.create(errorCode, errorText, failedUrl)
    }
    jbCefBrowser.setProperty(JBCefBrowser.Properties.FOCUS_ON_SHOW, true)
    jbCefBrowser.setProperty(JBCefBrowser.Properties.FOCUS_ON_NAVIGATION, true)
    jbCefBrowser.cefBrowser.client.removeRequestHandler()
    jbCefBrowser.cefBrowser.client.addRequestHandler(GBRequestHandler())

    setContent(jbCefBrowser.component)
    setToolbar(toolbar.component)


  }

  private fun buildToolbar(): DefaultActionGroup {
    val toolbar = DefaultActionGroup()
    val backButton = GBackAction(jbCefBrowser, AllIcons.Actions.Back)
    val forwardButton = GForwardAction(jbCefBrowser, AllIcons.Actions.Forward)
    val refreshButton = GRefreshAction(jbCefBrowser, AllIcons.Actions.Refresh)
    val homeButton = GHomeAction(jbCefBrowser, AllIcons.Nodes.HomeFolder)
    val bookMarksMenuAction = GBookMarksMenuAction(jbCefBrowser)
    val gBrowserOptionsActionGroup = GBrowserOptionsActionGroup(jbCefBrowser)


    val bus = ApplicationManager.getApplication().messageBus
    bus.connect().subscribe(SettingsChangedAction.TOPIC, object : SettingsChangedAction {
      override fun settingsChanged() {
        contentCs.launch {
          try {
            bookMarksMenuAction.updateView()
          }
          catch (e: Exception) {
            AllIcons.General.Web
          }

        }
      }
    })


    val urlTextField = GSearchFieldAction(initialUrl, "Web address", AllIcons.Actions.Refresh, jbCefBrowser, callback, contentCs)

    jbCefBrowser.cefBrowser.client.addDisplayHandler(CefUrlChangeHandler { url -> urlTextField.setText(url ?: "") })

    toolbar.add(backButton)
    toolbar.add(forwardButton)
    toolbar.add(refreshButton)
    toolbar.add(homeButton)
    toolbar.add(bookMarksMenuAction)
    toolbar.addSeparator()
    toolbar.add(urlTextField)
    toolbar.addSeparator()
    toolbar.add(gBrowserOptionsActionGroup, Constraints.LAST)

    return toolbar
  }


  override fun dispose() {
    jbCefBrowser.dispose()
  }
}
