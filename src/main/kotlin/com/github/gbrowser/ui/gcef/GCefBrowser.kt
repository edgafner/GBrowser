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
import com.github.gbrowser.ui.toolwindow.dev_tools.GBrowserToolWindowDevToolsFactory
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import com.github.gbrowser.util.GBrowserThemeUtil
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefClient
import com.intellij.util.application
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.callback.CefMenuModel.MenuId
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


  val devTools: CefBrowser?
    get() {
      return cefBrowser.devTools
    }

  val client: JBCefClient
    get() {
      return jbCefClient
    }
  private var titleChangeDelegate: GBrowserCefBrowserTitleDelegate? = null
  private var devToolsDelegates: MutableList<GBrowserCefDevToolsListener> = mutableListOf()

  init {
    setProperty("JBCefBrowser.focusOnShow", true)
    setProperty("JBCefBrowser.focusOnNavigation", true)
    setErrorPage { errorCode, errorText, failedUrl ->
      if (errorCode == CefLoadHandler.ErrorCode.ERR_ABORTED) null
      else GBrowserErrorPage.create(errorCode, errorText, failedUrl)
    }

    // Add load handler to apply theme and anti-detection measures
    jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
      override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        if (frame.isMain && httpStatusCode < 400) {
          // Apply theme and anti-detection measures after page loads successfully
          // Add a small delay to ensure the DOM is ready
          javax.swing.Timer(50) {
            GBrowserThemeUtil.applyTheme(browser, project)
            applyAntiDetectionMeasures(browser)
          }.apply {
            isRepeats = false
            start()
          }
        }
      }

      override fun onLoadStart(browser: CefBrowser, frame: CefFrame, transitionType: CefRequest.TransitionType) {
        if (frame.isMain) {
          // Apply theme and anti-detection measures at the start of loading to minimize flashing
          GBrowserThemeUtil.applyTheme(browser, project)
          applyAntiDetectionMeasures(browser)
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

        val settings = project.service<GBrowserService>()
        val openInDialog = settings.isDevToolsInDialog
        thisLogger().debug("isDevToolsInDialog setting: $openInDialog")

        if (openInDialog) {
          // Open in dialog using JCEF's built-in DevTools dialog
          thisLogger().debug("Opening DevTools in dialog")
          // Use JCEF to open DevTools in a new window/dialog
          val devToolsBrowser = cefBrowser.devTools
          val frame = javax.swing.JFrame("DevTools - ${cefBrowser.url}")
          frame.defaultCloseOperation = javax.swing.WindowConstants.DISPOSE_ON_CLOSE
          frame.add(devToolsBrowser.uiComponent)
          frame.setSize(1024, 768)
          frame.setLocationRelativeTo(null)
          frame.isVisible = true
        } else {
          // Open in the tool window
          val selectedBrowser = GBrowserToolWindowUtil.getSelectedBrowserPanel(project)
          if (selectedBrowser == null) {
            thisLogger().debug("No selected browser panel found")
            return
          }

          thisLogger().debug("Getting DevTools browser instance")
          val browser = selectedBrowser.getDevToolsBrowser()
          thisLogger().debug("Got DevTools browser: $browser")

          application.invokeLater {
            thisLogger().debug("Opening DevTools in the tool window")
            GBrowserToolWindowDevToolsFactory.Companion.createTab(project, browser, selectedBrowser.getCurrentTitle())
          }
        }
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

    // Reapply theme after resize to ensure it's properly rendered
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

  fun addTitleChangeListener(delegate: GBrowserCefBrowserTitleDelegate) {
    titleChangeDelegate = delegate
  }

  fun removeTitleChangeListener() {
    titleChangeDelegate = null
  }

  fun notifyTitleChanged(title: String?) {
    titleChangeDelegate?.onChangeTitle(title)
  }

  fun addDevToolsListener(listener: GBrowserCefDevToolsListener) {
    devToolsDelegates.add(listener)
  }

  fun removeDevToolsListener() {
    devToolsDelegates = mutableListOf()
  }

  fun disposeDevTools() {
    devTools?.close(true)
    devToolsDelegates.forEach {
      it.onDisposeDevtools()
    }
    removeDevToolsListener()
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
   * Apply anti-detection measures to make the browser appear more legitimate
   * and bypass bot detection systems like Cloudflare
   */
  private fun applyAntiDetectionMeasures(browser: CefBrowser) {
    try {
      val antiDetectionScript = """
        (function() {
          // Only apply anti-detection to sites that specifically need it
          const needsAntiDetection = [
            'perplexity.ai',
            'challenges.cloudflare.com',
            'openai.com',
            'chat.openai.com',
            'claude.ai'
          ];
          
          const currentHost = location.hostname.toLowerCase();
          const needsProtection = needsAntiDetection.some(domain => 
            currentHost.includes(domain) || currentHost.endsWith(domain)
          );
          
          if (!needsProtection) {
            return; // Skip anti-detection for most sites
          }
          
          // Remove automation indicators
          if (typeof window.webdriver !== 'undefined') {
            delete window.webdriver;
          }
          
          // Hide automation properties
          Object.defineProperty(navigator, 'webdriver', {
            get: () => undefined,
            configurable: true
          });
          
          // Hide automation flags
          ['__webdriver_evaluate', '__selenium_evaluate', '__webdriver_script_function', 
           '__webdriver_script_func', '__webdriver_script_fn', '__fxdriver_evaluate',
           '__driver_unwrapped', '__webdriver_unwrapped', '__driver_evaluate',
           '__selenium_unwrapped', '__fxdriver_unwrapped'].forEach(prop => {
            if (window[prop]) {
              delete window[prop];
            }
          });
          
          // Spoof Chrome runtime
          if (!window.chrome) {
            window.chrome = {};
          }
          
          if (!window.chrome.runtime) {
            Object.defineProperty(window.chrome, 'runtime', {
              get: () => ({
                onConnect: undefined,
                onMessage: undefined,
                sendMessage: () => {},
                connect: () => ({
                  onMessage: { addListener: () => {}, removeListener: () => {} },
                  onDisconnect: { addListener: () => {}, removeListener: () => {} },
                  postMessage: () => {}
                })
              }),
              configurable: true
            });
          }
          
          // Enhanced plugins spoofing
          Object.defineProperty(navigator, 'plugins', {
            get: () => {
              const plugins = [
                { name: 'Chrome PDF Plugin', description: 'Portable Document Format', filename: 'internal-pdf-viewer', length: 1 },
                { name: 'Chrome PDF Viewer', description: 'Portable Document Format', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', length: 1 },
                { name: 'Native Client', description: 'Native Client Executable', filename: 'internal-nacl-plugin', length: 2 }
              ];
              return Object.setPrototypeOf(plugins, PluginArray.prototype);
            },
            configurable: true
          });
          
          // Mock WebGL to avoid fingerprinting
          const getParameter = WebGLRenderingContext.prototype.getParameter;
          WebGLRenderingContext.prototype.getParameter = function(parameter) {
            if (parameter === 37445) {
              return 'Intel Inc.';
            }
            if (parameter === 37446) {
              return 'Intel(R) Iris(TM) Graphics 6100';
            }
            return getParameter.call(this, parameter);
          };
          
          // Override permissions API for better compatibility
          if (navigator.permissions && navigator.permissions.query) {
            const originalQuery = navigator.permissions.query;
            navigator.permissions.query = (parameters) => {
              return parameters.name === 'notifications' ?
                Promise.resolve({ state: 'default' }) :
                originalQuery.call(navigator.permissions, parameters);
            };
          }
          
          // Mock realistic connection
          Object.defineProperty(navigator, 'connection', {
            get: () => ({
              effectiveType: '4g',
              rtt: 50 + Math.random() * 100,
              downlink: 5 + Math.random() * 20,
              saveData: false
            }),
            configurable: true
          });
          
          // Mock battery with realistic values
          if (!navigator.getBattery) {
            navigator.getBattery = () => Promise.resolve({
              charging: Math.random() > 0.5,
              chargingTime: Math.random() * 3600,
              dischargingTime: 14400 + Math.random() * 7200,
              level: 0.5 + Math.random() * 0.5
            });
          }
          
          // Consistent language settings
          Object.defineProperty(navigator, 'language', {
            get: () => 'en-US',
            configurable: true
          });
          
          Object.defineProperty(navigator, 'languages', {
            get: () => ['en-US', 'en'],
            configurable: true
          });
          
          // Realistic hardware specs
          Object.defineProperty(navigator, 'hardwareConcurrency', {
            get: () => Math.max(2, Math.min(16, navigator.hardwareConcurrency || 4)),
            configurable: true
          });
          
          // Memory info spoofing
          Object.defineProperty(navigator, 'deviceMemory', {
            get: () => 8,
            configurable: true
          });
          
          // Hide automation in console
          const originalConsoleDebug = console.debug;
          console.debug = function(...args) {
            if (args.length > 0 && typeof args[0] === 'string' && 
                (args[0].includes('DevTools') || args[0].includes('automation'))) {
              return;
            }
            return originalConsoleDebug.apply(console, args);
          };
          
          // Mouse and keyboard event simulation
          ['mouse', 'keyboard'].forEach(eventType => {
            const events = eventType === 'mouse' ? 
              ['click', 'mousedown', 'mouseup', 'mousemove'] : 
              ['keydown', 'keyup', 'keypress'];
            
            events.forEach(event => {
              const originalAddEventListener = EventTarget.prototype.addEventListener;
              EventTarget.prototype.addEventListener = function(type, listener, options) {
                if (type === event && typeof listener === 'function') {
                  const wrappedListener = function(e) {
                    // Add realistic timing
                    if (!e.isTrusted) {
                      Object.defineProperty(e, 'isTrusted', { value: true, configurable: false });
                    }
                    return listener.call(this, e);
                  };
                  return originalAddEventListener.call(this, type, wrappedListener, options);
                }
                return originalAddEventListener.call(this, type, listener, options);
              };
            });
          });
        })();
      """.trimIndent()

      browser.executeJavaScript(antiDetectionScript, "", 0)
    } catch (e: Exception) {
      thisLogger().warn("Failed to apply anti-detection measures", e)
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
    removeDisplayHandler()
    removeRequestHandler()
    devToolsDelegates.forEach {
      it.onDisposeDevtools()
    }
    removeDevToolsListener()
    removeLifeSpanHandler()

    // Clean up device emulation state
    GBrowserMobileToggleAction.cleanupBrowserState(id)

    // Ensure browser is properly closed
    if (!cefBrowser.isClosing) {
      cefBrowser.close(true)
    }

    super.dispose()
  }
}