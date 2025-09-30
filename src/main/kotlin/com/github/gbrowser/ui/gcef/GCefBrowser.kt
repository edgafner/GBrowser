package com.github.gbrowser.ui.gcef

import com.github.gbrowser.actions.DeviceEmulationConstants
import com.github.gbrowser.actions.GBrowserMobileToggleAction
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.services.providers.CachingWebPageTitleLoader
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.gcef.impl.GBrowserCefDisplayChangeHandler
import com.github.gbrowser.ui.gcef.impl.GBrowserCefLifeSpanDelegate
import com.github.gbrowser.ui.gcef.impl.GBrowserCefRequestHandler
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import com.github.gbrowser.util.GBrowserThemeUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.callback.CefMenuModel.MenuId
import org.cef.handler.CefLifeSpanHandlerAdapter
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefCookieManager
import org.cef.network.CefRequest
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
class GCefBrowser(val project: Project, url: String?, client: JBCefClient? = null, browser: CefBrowser? = null, val id: String = UUID.randomUUID().toString()) :
  JBCefBrowser(createBuilderForProject().apply {
    setOffScreenRendering(false)
    setEnableOpenDevToolsMenuItem(true)
    setCefBrowser(browser)
    setClient(client)
    setUrl(url)
  }) {

  companion object {
    private fun createBuilderForProject(): JBCefBrowserBuilder {
      val builder = createBuilder()

      // Note: CEF command line arguments for dark mode need to be set globally
      // before CEF is initialized. Since JCEF is already initialized by IntelliJ,
      // we use JavaScript injection instead. In a standalone application, you would use:
      // --force-dark-mode --enable-features=WebUIDarkMode

      return builder
    }
  }

  private val favIconLoader: CachingWebPageTitleLoader = service()


  private var titleChangeDelegate: GBrowserCefBrowserTitleDelegate? = null

  init {
    setProperty("JBCefBrowser.focusOnShow", false)
    setProperty("JBCefBrowser.focusOnNavigation", false)
    setErrorPage { errorCode, errorText, failedUrl ->
      if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) null
      else GBrowserErrorPage.create(errorCode, errorText, failedUrl)
    }

    // Add a lifecycle handler to inject anti-detection measures once when the browser is created
    jbCefClient.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
      override fun onAfterCreated(browser: CefBrowser) {
        // Apply browser compatibility mode if enabled
        val settings = project.service<GBrowserService>()
        if (settings.antiDetectionEnabled) {
          thisLogger().info("GBrowser: Browser compatibility mode enabled for browser $id")
          applyAntiDetectionMeasures(browser)
        } else {
          thisLogger().debug("GBrowser: Browser compatibility mode is disabled for browser $id")
        }
      }
    }, cefBrowser)

    // Add a load handler to apply the theme
    jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
      override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        if (frame.isMain && httpStatusCode < 400) {
          // Apply theme after the page loads successfully
          // Add a small delay to ensure the DOM is ready
          javax.swing.Timer(50) {
            GBrowserThemeUtil.applyTheme(browser, project)
          }.apply {
            isRepeats = false
            start()
          }
        }
      }

      override fun onLoadStart(browser: CefBrowser, frame: CefFrame, transitionType: CefRequest.TransitionType) {
        if (frame.isMain) {
          // Apply theme at the start of loading to minimize flashing
          GBrowserThemeUtil.applyTheme(browser, project)
        }
      }
    }, cefBrowser)

    // Register this browser with the lifecycle manager
    GCefBrowserLifecycleManager.getInstance().registerBrowser(this)
  }


  override fun createDefaultContextMenuHandler(): DefaultCefContextMenuHandler {
    return object : DefaultCefContextMenuHandler(true) {
      private val BOOKMARK_ADD: Int = 26501

      override fun onBeforeContextMenu(browser: CefBrowser, frame: CefFrame, params: CefContextMenuParams, model: CefMenuModel) {
        if (isProperty(Properties.NO_CONTEXT_MENU)) {
          model.clear()
          return
        }

        model.addItem(MenuId.MENU_ID_USER_LAST, "Open DevTools")
        model.addItem(
          BOOKMARK_ADD, GBrowserBundle.message(
            "action.GBrowserBookmarkAddAction.text"
          )
        )
      }

      override fun onContextMenuCommand(browser: CefBrowser, frame: CefFrame, params: CefContextMenuParams, commandId: Int, eventFlags: Int): Boolean {
        if (commandId == BOOKMARK_ADD) {
          addToBookmarks(browser)
          return true
        }
        if (commandId == MenuId.MENU_ID_USER_LAST) {
          openDevtools()
          return true
        }
        return super.onContextMenuCommand(browser, frame, params, commandId, eventFlags)
      }

      private fun addToBookmarks(browser: CefBrowser) {
        favIconLoader.getTitleOfWebPage(browser.url).thenAccept {
          project.service<GBrowserService>().addBookmarks(GBrowserBookmark(browser.url, it))
        }
      }

      private fun openDevtools() {
        thisLogger().debug("GCefBrowser.openDevtools() called from context menu")
        // In the new API (253 EAP), DevTools can only be opened in a built-in dialog
        thisLogger().debug("Opening DevTools using a built-in dialog (new API)")
        this@GCefBrowser.openDevtools()
      }
    }
  }

  fun setVisibility(isVisible: Boolean) {
    component.isVisible = isVisible

    // Also ensure the browser UI component is visible
    // This addresses specific unpinned mode rendering issues
    cefBrowser.uiComponent?.isVisible = isVisible

    // Force a repaint if becoming visible
    if (isVisible) {
      component.invalidate()
      component.validate()
      component.repaint()

      // Also repaint the UI component
      cefBrowser.uiComponent?.invalidate()
      cefBrowser.uiComponent?.validate()
      cefBrowser.uiComponent?.repaint()

      // Invalidate the browser to trigger proper rendering
      // This forces a repaint of the CEF browser component when visibility changes
      // to prevent rendering artifacts and ensure the browser content is properly displayed
      try {
        cefBrowser.invalidate()
      } catch (e: Exception) {
        thisLogger().warn("Failed to invalidate CEF browser during visibility change", e)
      }

      // Ensure parent containers are refreshed too
      val parent = component.parent
      parent?.invalidate()
      parent?.validate()
      parent?.repaint()
    }
  }

  fun forceResize() {
    // Force the browser to recalculate its size
    component.apply {
      invalidate()
      revalidate()
      repaint()
    }

    // Also update the CEF browser UI component
    cefBrowser.uiComponent?.apply {
      invalidate()
      revalidate()
      repaint()
    }

    // Invalidate the browser to trigger resize handling
    // This ensures that the CEF browser properly handles component resizing
    // by forcing a repaint after size changes to prevent display corruption
    try {
      cefBrowser.invalidate()
    } catch (e: Exception) {
      thisLogger().warn("Failed to invalidate CEF browser during resize", e)
    }

    // Reapply the theme after resize to ensure it's properly rendered
    javax.swing.Timer(DeviceEmulationConstants.THEME_UPDATE_DELAY_MS) {
      GBrowserThemeUtil.applyTheme(cefBrowser, project)
    }.apply {
      isRepeats = false
      start()
    }
  }

  fun deleteCookies() {
    val manager = CefCookieManager.getGlobalManager()
    manager.deleteCookies(null, null)
  }


  fun notifyTitleChanged(title: String?) {
    titleChangeDelegate?.onChangeTitle(title)
  }


  fun refreshTheme() {
    // Add a small delay to ensure the page is ready
    javax.swing.Timer(DeviceEmulationConstants.THEME_UPDATE_DELAY_MS) {
      GBrowserThemeUtil.applyTheme(cefBrowser, project)
    }.apply {
      isRepeats = false
      start()
    }
  }

  /**
   * Apply browser compatibility mode to enable the IDE browser to work with
   * sites that incorrectly identify it as a bot. This helps developers access
   * documentation, cloud consoles, and test their own applications.
   * Note: This is only injected once per browser session for performance
   */
  private fun applyAntiDetectionMeasures(browser: CefBrowser) {
    try {
      thisLogger().debug("GBrowser: Applying browser compatibility mode for browser $id")

      // Load the compatibility script template from resources
      val scriptTemplate = this::class.java.getResourceAsStream("/scripts/anti-detection.js")?.bufferedReader()?.readText()
        ?: run {
          thisLogger().warn("GBrowser: Could not load the browser compatibility script")
          return
        }

      // Get the configurable sites from settings
      val settings = project.service<GBrowserService>()
      val sites = settings.antiDetectionSites

      // Convert sites to JavaScript array format
      val sitesJs = sites.joinToString(separator = ",\n            ") { "'$it'" }

      // Replace placeholder with actual sites
      // The placeholder is in a comment to keep IDE's JS parser happy
      val antiDetectionScript = scriptTemplate.replace("/* %SITES_PLACEHOLDER% */", "[\n            $sitesJs\n          ]")

      // Execute the compatibility script
      browser.executeJavaScript(antiDetectionScript, "", 0)

      thisLogger().info("GBrowser: Browser compatibility mode applied for ${sites.size} sites")
    } catch (e: Exception) {
      thisLogger().error("GBrowser: Failed to apply browser compatibility mode", e)
    }
  }

  fun addDisplayHandler(delegate: GBrowserToolWindowActionBarDelegate) {
    cefBrowser.client?.addDisplayHandler(GBrowserCefDisplayChangeHandler(delegate))
  }

  fun addLifeSpanHandler(toolWindow: ToolWindow) {
    cefBrowser.client?.addLifeSpanHandler(GBrowserCefLifeSpanDelegate(project, toolWindow))
  }

  fun removeDisplayHandler() {
    cefBrowser.client?.removeDisplayHandler()
  }

  fun removeLifeSpanHandler() {
    cefBrowser.client?.removeLifeSpanHandler()
  }

  fun addRequestHandler(handler: GBrowserCefRequestHandler) {
    cefBrowser.client?.removeRequestHandler()
    cefBrowser.client?.addRequestHandler(handler)
  }

  fun removeRequestHandler() {
    cefBrowser.client?.removeRequestHandler()
  }

  override fun dispose() {
    // Use defensive error handling to ensure all cleanup steps are attempted
    // even if some fail, preventing resource leaks

    try {
      removeDisplayHandler()
    } catch (e: Exception) {
      thisLogger().warn("GBrowser: Failed to remove display handler during disposal", e)
    }

    try {
      removeRequestHandler()
    } catch (e: Exception) {
      thisLogger().warn("GBrowser: Failed to remove request handler during disposal", e)
    }

    try {
      // DevTools cleanup removed - no longer available in the new API
      removeLifeSpanHandler()
    } catch (e: Exception) {
      thisLogger().warn("GBrowser: Failed to remove life span handler during disposal", e)
    }

    try {
      // Clean up device emulation state
      GBrowserMobileToggleAction.cleanupBrowserState(id)
    } catch (e: Exception) {
      thisLogger().warn("GBrowser: Failed to clean up browser state during disposal", e)
    }

    try {
      // Stop any loading operations before closing.
      // This is necessary because if the browser is disposed while a page is still loading,
      // it can lead to resource leaks or exceptions. Stopping the load ensures that all
      // network and rendering activity is halted before disposal, following best practices
      // established in the JBCefBrowserBase implementation.
      cefBrowser.stopLoad()
    } catch (e: Exception) {
      thisLogger().warn("GBrowser: Failed to stop browser loading during disposal", e)
    }

    try {
      // Allow the browser to close. This is necessary because, by default, the CEF browser may prevent closing unless explicitly allowed.
      // Following the JBCefBrowserBase pattern, we call setCloseAllowed() to ensure the browser can be closed safely during disposal.
      // Omitting this step may result in the browser not closing properly, leading to resource leaks or hanging processes.
      cefBrowser.setCloseAllowed()
    } catch (e: Exception) {
      thisLogger().warn("GBrowser: Failed to set close allowed during disposal", e)
    }

    try {
      // Ensure the browser is properly closed
      if (!cefBrowser.isClosing) {
        cefBrowser.close(true)
      }
    } catch (e: Exception) {
      thisLogger().error("GBrowser: Failed to close CEF browser during disposal", e)
    }

    try {
      super.dispose()
    } catch (e: Exception) {
      thisLogger().error("GBrowser: Failed to call super.dispose()", e)
    }
  }

}