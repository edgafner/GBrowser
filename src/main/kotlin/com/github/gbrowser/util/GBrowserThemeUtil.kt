package com.github.gbrowser.util

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.theme.GBrowserTheme
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import org.cef.browser.CefBrowser

object GBrowserThemeUtil {

  private val logger = thisLogger()

  // Cache for compiled JavaScript to avoid repeated string concatenation
  private var cachedDarkModeScript: String? = null
  private var cachedLightModeScript: String? = null

  // Constant for JavaScript execution line
  private const val INLINE_SCRIPT_LINE = 0

  fun isDarkTheme(project: Project): Boolean {
    val settings = project.service<GBrowserService>()
    return when (settings.theme) {
      GBrowserTheme.FOLLOW_IDE -> JBColor.isBright().not()
      GBrowserTheme.DARK -> true
      GBrowserTheme.LIGHT -> false
    }
  }

  private fun getDarkModeScript(): String {
    if (cachedDarkModeScript == null) {
      cachedDarkModeScript = buildDarkModeScript()
    }
    return cachedDarkModeScript!!
  }

  private fun getLightModeScript(): String {
    if (cachedLightModeScript == null) {
      cachedLightModeScript = buildLightModeScript()
    }
    return cachedLightModeScript!!
  }

  fun applyTheme(browser: CefBrowser, project: Project) {
    val isDark = isDarkTheme(project)
    logger.debug("GBrowserThemeUtil: Applying theme - isDark=$isDark")

    try {
      // First, try to use Chrome DevTools Protocol to set dark mode preference
      // This is the most reliable way to ensure proper dark mode support
      val devToolsScript = if (isDark) {
        """
        (function() {
          // Use Chrome DevTools Protocol to enable dark mode
          if (window.chrome && window.chrome.send) {
            try {
              // This simulates the Chrome DevTools Protocol command
              window.chrome.send('setEmulatedMedia', [{
                media: 'prefers-color-scheme: dark'
              }]);
            } catch (e) {
              console.log('[GBrowser] DevTools Protocol not available');
            }
          }
        })();
        """
      } else {
        """
        (function() {
          // Use Chrome DevTools Protocol to disable dark mode
          if (window.chrome && window.chrome.send) {
            try {
              // This simulates the Chrome DevTools Protocol command
              window.chrome.send('setEmulatedMedia', [{
                media: 'prefers-color-scheme: light'
              }]);
            } catch (e) {
              console.log('[GBrowser] DevTools Protocol not available');
            }
          }
        })();
        """
      }

      // Execute DevTools script first
      browser.executeJavaScript(devToolsScript.trimIndent(), browser.url, INLINE_SCRIPT_LINE)

      // Then apply our theme script as a fallback
      val script = if (isDark) getDarkModeScript() else getLightModeScript()
      browser.executeJavaScript(script, browser.url, INLINE_SCRIPT_LINE)
    } catch (e: Exception) {
      logger.error("GBrowserThemeUtil: Failed to apply theme", e)
    }
  }

  private fun buildDarkModeScript(): String = """
                (function() {
                    console.log('[GBrowser] Applying dark theme');
                    
                    // Check if device emulation is active
                    if (window.__gbrowserDarkModeApplied === 'device-emulation') {
                        console.log('[GBrowser] Device emulation active, skipping dark theme');
                        return;
                    }
                    
                    // Check if already applied
                    if (window.__gbrowserDarkModeApplied === true) {
                        console.log('[GBrowser] Dark theme already applied');
                        return;
                    }
                    window.__gbrowserDarkModeApplied = true;
                    
                    // Force dark color scheme using multiple methods
                    if (window.matchMedia) {
                        // Store original matchMedia
                        if (!window.__originalMatchMedia) {
                            window.__originalMatchMedia = window.matchMedia;
                        }
                        
                        // Override matchMedia to always report dark mode
                        window.matchMedia = function(query) {
                            if (query === '(prefers-color-scheme: dark)') {
                                return {
                                    matches: true,
                                    media: query,
                                    onchange: null,
                                    addListener: function() {},
                                    removeListener: function() {},
                                    addEventListener: function() {},
                                    removeEventListener: function() {},
                                    dispatchEvent: function() { return true; }
                                };
                            }
                            return window.__originalMatchMedia.call(window, query);
                        };
                    }
                    
                    // Add meta tags for color scheme
                    let metaColorScheme = document.querySelector('meta[name="color-scheme"]');
                    if (!metaColorScheme) {
                        metaColorScheme = document.createElement('meta');
                        metaColorScheme.name = 'color-scheme';
                        document.head.appendChild(metaColorScheme);
                    }
                    metaColorScheme.content = 'dark';
                    
                    // Set theme color
                    let metaTheme = document.querySelector('meta[name="theme-color"]');
                    if (!metaTheme) {
                        metaTheme = document.createElement('meta');
                        metaTheme.name = 'theme-color';
                        document.head.appendChild(metaTheme);
                    }
                    metaTheme.content = '#202124';
                    
                    // Add minimal dark mode CSS for sites that don't support it natively
                    let darkStyle = document.getElementById('gbrowser-dark-mode-support');
                    if (!darkStyle) {
                        darkStyle = document.createElement('style');
                        darkStyle.id = 'gbrowser-dark-mode-support';
                        darkStyle.innerHTML = `
                            /* Only apply minimal styles to sites that don't already support dark mode */
                            @media not all and (prefers-color-scheme: dark) {
                                :root {
                                    color-scheme: dark;
                                }
                            }
                            
                            /* Chrome-like scrollbar styling for dark mode */
                            @media (prefers-color-scheme: dark) {
                                ::-webkit-scrollbar {
                                    width: 16px;
                                    height: 16px;
                                }
                                
                                ::-webkit-scrollbar-track {
                                    background-color: #202124;
                                }
                                
                                ::-webkit-scrollbar-thumb {
                                    background-color: #35363a;
                                    border-radius: 8px;
                                    border: 4px solid #202124;
                                }
                                
                                ::-webkit-scrollbar-thumb:hover {
                                    background-color: #5f6368;
                                }
                            }
                        `;
                        document.head.appendChild(darkStyle);
                    }
                    
                    // Dispatch event to notify the page about color scheme change
                    window.dispatchEvent(new Event('colorschemechange'));
                    
                    // For sites that use JavaScript to detect dark mode
                    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                        window.matchMedia('(prefers-color-scheme: dark)').dispatchEvent(new Event('change'));
                    }
                    
                    console.log('[GBrowser] Dark theme applied successfully');
                })();
            """.trimIndent()

  private fun buildLightModeScript(): String = """
                (function() {
                    console.log('[GBrowser] Applying light theme');
                    
                    // Check if device emulation is active
                    if (window.__gbrowserDarkModeApplied === 'device-emulation') {
                        console.log('[GBrowser] Device emulation active, skipping light theme');
                        return;
                    }
                    
                    window.__gbrowserDarkModeApplied = false;
                    
                    // Restore original matchMedia
                    if (window.__originalMatchMedia) {
                        window.matchMedia = window.__originalMatchMedia;
                        console.log('[GBrowser] Restored original matchMedia');
                    }
                    
                    // Update meta tags
                    const metaColorScheme = document.querySelector('meta[name="color-scheme"]');
                    if (metaColorScheme) {
                        metaColorScheme.content = 'light';
                    }
                    
                    const metaTheme = document.querySelector('meta[name="theme-color"]');
                    if (metaTheme) {
                        metaTheme.content = '#ffffff';
                    }
                    
                    // Remove dark mode support CSS
                    const darkStyle = document.getElementById('gbrowser-dark-mode-support');
                    if (darkStyle) {
                        darkStyle.remove();
                        console.log('[GBrowser] Removed dark mode CSS');
                    }
                    
                    // Dispatch events
                    window.dispatchEvent(new Event('colorschemechange'));
                    
                    // For sites that use JavaScript to detect light mode
                    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
                        window.matchMedia('(prefers-color-scheme: light)').dispatchEvent(new Event('change'));
                    }
                    
                    console.log('[GBrowser] Light theme applied successfully');
                })();
            """.trimIndent()
}