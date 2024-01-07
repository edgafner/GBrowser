package com.github.gbrowser.actions.browser

import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.GeneralLocalSettings
import com.intellij.ide.browsers.BrowserLauncherAppless
import com.intellij.ide.browsers.DefaultBrowserPolicy
import com.intellij.ide.browsers.WebBrowserManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

class GBrowserOpenInAction : AnAction(), DumbAware {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.hasContent() ?: false
    findUsingBrowser().let {
      e.presentation.icon = it
    }


  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun actionPerformed(e: AnActionEvent) {
    GBrowserToolWindowUtil.getSelectedBrowserPanel(e)?.getCurrentUrl()?.let { BrowserUtil.browse(it) }
  }

  private fun findUsingBrowser(): Icon {
    val browserManager = WebBrowserManager.getInstance()
    val defaultBrowserPolicy = browserManager.defaultBrowserPolicy
    if (defaultBrowserPolicy == DefaultBrowserPolicy.FIRST) {
      return browserManager.firstActiveBrowser?.icon ?: AllIcons.Xml.Browsers.Chrome
    } else if (defaultBrowserPolicy == DefaultBrowserPolicy.SYSTEM && BrowserLauncherAppless.canUseSystemDefaultBrowserPolicy()) {
      return AllIcons.Xml.Browsers.Chrome
    } else if (defaultBrowserPolicy == DefaultBrowserPolicy.ALTERNATIVE) {
      val path = GeneralLocalSettings.getInstance().browserPath
      if (path.isNotBlank()) {
        val browser = browserManager.findBrowserById(path)
        if (browser == null) {
          for (item in browserManager.activeBrowsers) {
            if (path == item.path) {
              return item.icon
            }
          }
        } else {
          return browser.icon
        }
      }
    }
    return AllIcons.Xml.Browsers.Chrome
  }
}
