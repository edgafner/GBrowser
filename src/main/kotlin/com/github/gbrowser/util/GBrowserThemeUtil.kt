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
                    
                    // Force dark color scheme
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
                    metaTheme.content = '#1e1e1e';
                    
                    // Add dark mode CSS
                    let darkStyle = document.getElementById('gbrowser-dark-mode-support');
                    if (!darkStyle) {
                        darkStyle = document.createElement('style');
                        darkStyle.id = 'gbrowser-dark-mode-support';
                        darkStyle.innerHTML = `
                            :root {
                                color-scheme: dark;
                            }
                            
                            /* Apply dark theme filter to html element */
                            html {
                                filter: invert(1) hue-rotate(180deg);
                                background-color: #121212 !important;
                            }
                            
                            /* Revert filter for images and videos to preserve original colors */
                            img, video, iframe, canvas, embed, object {
                                filter: invert(1) hue-rotate(180deg);
                            }
                            
                            /* Basic dark theme colors */
                            body {
                                background-color: #121212 !important;
                                color: #e0e0e0 !important;
                            }
                            
                            /* Scrollbar styling */
                            ::-webkit-scrollbar {
                                background-color: #2d2d2d;
                            }
                            
                            ::-webkit-scrollbar-thumb {
                                background-color: #555;
                            }
                            
                            ::-webkit-scrollbar-track {
                                background-color: #2d2d2d;
                            }
                        `;
                        document.head.appendChild(darkStyle);
                    }
                    
                    // Dispatch event to notify the page about color scheme change
                    window.dispatchEvent(new Event('colorschemechange'));
                    
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
                        metaColorScheme.content = 'light dark';
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
                    
                    // Add light mode CSS to ensure proper styling
                    let lightStyle = document.getElementById('gbrowser-light-mode-support');
                    if (!lightStyle) {
                        lightStyle = document.createElement('style');
                        lightStyle.id = 'gbrowser-light-mode-support';
                        lightStyle.innerHTML = `
                            :root {
                                color-scheme: light;
                            }
                            
                            /* Ensure light theme colors */
                            html {
                                background-color: #ffffff !important;
                            }
                            
                            body {
                                background-color: #ffffff !important;
                                color: #000000 !important;
                            }
                        `;
                        document.head.appendChild(lightStyle);
                    }
                    
                    // Dispatch event
                    window.dispatchEvent(new Event('colorschemechange'));
                    
                    console.log('[GBrowser] Light theme applied successfully');
                })();
            """.trimIndent()
}