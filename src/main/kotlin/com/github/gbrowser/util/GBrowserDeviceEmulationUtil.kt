package com.github.gbrowser.util

import com.github.gbrowser.actions.DeviceEmulationConstants
import com.intellij.openapi.diagnostic.thisLogger
import org.cef.browser.CefBrowser

data class DeviceProfile(
  val name: String,
  val width: Int,
  val height: Int,
  val deviceScaleFactor: Double,
  val userAgent: String,
  val isMobile: Boolean = true,
  val hasTouch: Boolean = true
)

object GBrowserDeviceEmulationUtil {

  private val LOG = thisLogger()

  private const val INLINE_SCRIPT_LINE = 0

  /**
   * Escapes a string for safe inclusion in JavaScript string literals.
   * Prevents XSS attacks by escaping quotes, backslashes, and newlines.
   */
  private fun escapeJavaScriptString(input: String): String {
    return input
      .replace("\\", "\\\\")  // Escape backslashes first
      .replace("'", "\\'")    // Escape single quotes
      .replace("\"", "\\\"")  // Escape double quotes
      .replace("\n", "\\n")   // Escape newlines
      .replace("\r", "\\r")   // Escape carriage returns
      .replace("\t", "\\t")   // Escape tabs
      .replace("\u2028", "\\u2028") // Escape line separator
      .replace("\u2029", "\\u2029") // Escape paragraph separator
  }

  val DEVICE_PROFILES = mapOf(
    "iPhone SE" to DeviceProfile(
      name = "iPhone SE",
      width = 375,
      height = 667,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPHONE_SE
    ),
    "iPhone XR" to DeviceProfile(
      name = "iPhone XR",
      width = 414,
      height = 896,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPHONE_XR
    ),
    "iPhone 12 Pro" to DeviceProfile(
      name = "iPhone 12 Pro",
      width = 390,
      height = 844,
      deviceScaleFactor = 3.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPHONE_12_PRO
    ),
    "iPhone 14 Pro Max" to DeviceProfile(
      name = "iPhone 14 Pro Max",
      width = 430,
      height = 932,
      deviceScaleFactor = 3.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPHONE_XR
    ),
    "Pixel 7" to DeviceProfile(
      name = "Pixel 7",
      width = 412,
      height = 915,
      deviceScaleFactor = 2.625,
      userAgent = DeviceEmulationConstants.USER_AGENT_PIXEL_7
    ),
    "Samsung Galaxy S8+" to DeviceProfile(
      name = "Samsung Galaxy S8+",
      width = 360,
      height = 740,
      deviceScaleFactor = 4.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_SAMSUNG_S8_PLUS
    ),
    "Samsung Galaxy S20 Ultra" to DeviceProfile(
      name = "Samsung Galaxy S20 Ultra",
      width = 412,
      height = 915,
      deviceScaleFactor = 3.5,
      userAgent = DeviceEmulationConstants.USER_AGENT_SAMSUNG_S20_ULTRA
    ),
    "iPad Mini" to DeviceProfile(
      name = "iPad Mini",
      width = 768,
      height = 1024,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPAD_MINI
    ),
    "iPad Air" to DeviceProfile(
      name = "iPad Air",
      width = 820,
      height = 1180,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPAD_MINI
    ),
    "iPad Pro" to DeviceProfile(
      name = "iPad Pro",
      width = 1024,
      height = 1366,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_IPAD_MINI
    ),
    "Surface Pro 7" to DeviceProfile(
      name = "Surface Pro 7",
      width = 912,
      height = 1368,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_SURFACE_PRO_7
    ),
    "Surface Duo" to DeviceProfile(
      name = "Surface Duo",
      width = 540,
      height = 720,
      deviceScaleFactor = 2.5,
      userAgent = DeviceEmulationConstants.USER_AGENT_SURFACE_DUO
    ),
    "Galaxy Z Fold 5" to DeviceProfile(
      name = "Galaxy Z Fold 5",
      width = 344,
      height = 882,
      deviceScaleFactor = 3.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_GALAXY_Z_FOLD_5
    ),
    "Asus Zenbook Fold" to DeviceProfile(
      name = "Asus Zenbook Fold",
      width = 853,
      height = 1280,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_ASUS_ZENBOOK_FOLD
    ),
    "Samsung Galaxy A51/71" to DeviceProfile(
      name = "Samsung Galaxy A51/71",
      width = 412,
      height = 914,
      deviceScaleFactor = 2.625,
      userAgent = DeviceEmulationConstants.USER_AGENT_SAMSUNG_A51_71
    ),
    "Nest Hub" to DeviceProfile(
      name = "Nest Hub",
      width = 1024,
      height = 600,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_NEST_HUB
    ),
    "Nest Hub Max" to DeviceProfile(
      name = "Nest Hub Max",
      width = 1280,
      height = 800,
      deviceScaleFactor = 2.0,
      userAgent = DeviceEmulationConstants.USER_AGENT_NEST_HUB_MAX
    )
  )

  fun applyDeviceEmulation(browser: CefBrowser, profile: DeviceProfile) {
    LOG.info("GBrowserDeviceEmulationUtil: Applying device emulation for '${profile.name}' - ${profile.width}x${profile.height}, DPR: ${profile.deviceScaleFactor}")
    LOG.debug("GBrowserDeviceEmulationUtil: User agent: ${profile.userAgent}")
    LOG.debug("GBrowserDeviceEmulationUtil: isMobile: ${profile.isMobile}, hasTouch: ${profile.hasTouch}")

    // Escape values for safe JavaScript injection
    val escapedUserAgent = escapeJavaScriptString(profile.userAgent)
    val escapedName = escapeJavaScriptString(profile.name)

    // Apply device metrics override using JavaScript injection
    val js = """
            (function() {
                // Store original values for restoration
                if (!window.__gbrowserOriginalValues) {
                    window.__gbrowserOriginalValues = {
                        innerWidth: window.innerWidth,
                        innerHeight: window.innerHeight,
                        devicePixelRatio: window.devicePixelRatio,
                        userAgent: navigator.userAgent,
                        platform: navigator.platform,
                        maxTouchPoints: navigator.maxTouchPoints
                    };
                    console.log('[GBrowser] Stored original values:', window.__gbrowserOriginalValues);
                }
                
                // Override viewport dimensions
                Object.defineProperty(window, 'innerWidth', {
                    get: function() { return ${profile.width}; },
                    configurable: true
                });
                Object.defineProperty(window, 'innerHeight', {
                    get: function() { return ${profile.height}; },
                    configurable: true
                });
                Object.defineProperty(window, 'outerWidth', {
                    get: function() { return ${profile.width}; },
                    configurable: true
                });
                Object.defineProperty(window, 'outerHeight', {
                    get: function() { return ${profile.height}; },
                    configurable: true
                });
                
                // Override device pixel ratio
                Object.defineProperty(window, 'devicePixelRatio', {
                    get: function() { return ${profile.deviceScaleFactor}; },
                    configurable: true
                });
                
                // Override screen dimensions
                Object.defineProperty(screen, 'width', {
                    get: function() { return ${profile.width}; },
                    configurable: true
                });
                Object.defineProperty(screen, 'height', {
                    get: function() { return ${profile.height}; },
                    configurable: true
                });
                
                // Override navigator properties
                Object.defineProperty(navigator, 'userAgent', {
                    get: function() { return '${escapedUserAgent}'; },
                    configurable: true
                });
                
                // Override platform for mobile devices
                if (${profile.isMobile}) {
                    Object.defineProperty(navigator, 'platform', {
                        get: function() { 
                            if ('${escapedUserAgent}'.includes('iPhone') || '${escapedUserAgent}'.includes('iPad')) {
                                return 'iPhone';
                            }
                            return 'Linux armv8l';
                        },
                        configurable: true
                    });
                }
                
                // Override touch support
                Object.defineProperty(navigator, 'maxTouchPoints', {
                    get: function() { return ${if (profile.hasTouch) "5" else "0"}; },
                    configurable: true
                });
                
                // Add touch event support for mobile
                if (${profile.hasTouch} && !window.ontouchstart) {
                    window.ontouchstart = null;
                    window.ontouchmove = null;
                    window.ontouchend = null;
                    window.ontouchcancel = null;
                }
                
                // Update viewport meta-tag for mobile
                if (${profile.isMobile}) {
                    let viewport = document.querySelector('meta[name="viewport"]');
                    if (!viewport) {
                        viewport = document.createElement('meta');
                        viewport.name = 'viewport';
                        document.head.appendChild(viewport);
                    }
                    viewport.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                }
                
                // Dispatch resize event
                window.dispatchEvent(new Event('resize'));
                
                // Add mobile class to body for CSS targeting
                if (${profile.isMobile}) {
                    document.body.classList.add('gbrowser-mobile-emulation');
                } else {
                    document.body.classList.remove('gbrowser-mobile-emulation');
                }
                
                // Set flag to prevent dark theme from applying to content
                window.__gbrowserDarkModeApplied = 'device-emulation';
                
                // Add touch cursor styles for mobile devices
                if (${profile.hasTouch}) {
                    let touchStyle = document.getElementById('gbrowser-touch-cursor');
                    if (!touchStyle) {
                        touchStyle = document.createElement('style');
                        touchStyle.id = 'gbrowser-touch-cursor';
                        touchStyle.innerHTML = `
                            body.gbrowser-mobile-emulation * {
                                cursor: default !important;
                            }
                            body.gbrowser-mobile-emulation a,
                            body.gbrowser-mobile-emulation button,
                            body.gbrowser-mobile-emulation input[type="button"],
                            body.gbrowser-mobile-emulation input[type="submit"],
                            body.gbrowser-mobile-emulation [onclick],
                            body.gbrowser-mobile-emulation [role="button"] {
                                cursor: pointer !important;
                            }
                        `;
                        document.head.appendChild(touchStyle);
                    }
                }
                
                console.log('[GBrowser] Device emulation applied:', {
                    name: '${escapedName}',
                    width: ${profile.width},
                    height: ${profile.height},
                    devicePixelRatio: ${profile.deviceScaleFactor}
                });
            })();
        """.trimIndent()

    try {
      browser.executeJavaScript(js, "", INLINE_SCRIPT_LINE)
      LOG.info("GBrowserDeviceEmulationUtil: JavaScript executed for device emulation.\n  Current URL: ${browser.url}")
    } catch (e: Exception) {
      LOG.error("GBrowserDeviceEmulationUtil: Failed to apply device emulation", e)
    }
  }

  fun resetDeviceEmulation(browser: CefBrowser) {
    LOG.info("GBrowserDeviceEmulationUtil: Resetting device emulation")
    LOG.debug("GBrowserDeviceEmulationUtil: Current URL: ${browser.url}")

    val js = """
            (function() {
                // Restore original values if they exist
                if (window.__gbrowserOriginalValues) {
                    const orig = window.__gbrowserOriginalValues;
                    
                    // Restore window properties
                    Object.defineProperty(window, 'innerWidth', {
                        get: function() { return window.outerWidth; },
                        configurable: true
                    });
                    Object.defineProperty(window, 'innerHeight', {
                        get: function() { return window.outerHeight; },
                        configurable: true
                    });
                    Object.defineProperty(window, 'outerWidth', {
                        get: function() { return window.outerWidth; },
                        configurable: true
                    });
                    Object.defineProperty(window, 'outerHeight', {
                        get: function() { return window.outerHeight; },
                        configurable: true
                    });
                    Object.defineProperty(window, 'devicePixelRatio', {
                        get: function() { return orig.devicePixelRatio; },
                        configurable: true
                    });
                    
                    // Restore screen properties to actual screen dimensions
                    Object.defineProperty(screen, 'width', {
                        get: function() { return screen.width; },
                        configurable: true
                    });
                    Object.defineProperty(screen, 'height', {
                        get: function() { return screen.height; },
                        configurable: true
                    });
                    
                    // Restore navigator properties
                    Object.defineProperty(navigator, 'userAgent', {
                        get: function() { return orig.userAgent; },
                        configurable: true
                    });
                    Object.defineProperty(navigator, 'platform', {
                        get: function() { return orig.platform; },
                        configurable: true
                    });
                    Object.defineProperty(navigator, 'maxTouchPoints', {
                        get: function() { return orig.maxTouchPoints; },
                        configurable: true
                    });
                    
                    // Remove touch events if they were added
                    if (orig.maxTouchPoints === 0) {
                        delete window.ontouchstart;
                        delete window.ontouchmove;
                        delete window.ontouchend;
                        delete window.ontouchcancel;
                    }
                    
                    // Remove stored values
                    delete window.__gbrowserOriginalValues;
                }
                
                // Remove mobile class
                document.body.classList.remove('gbrowser-mobile-emulation');
                
                // Clear the device emulation flag to allow themes to be applied again
                if (window.__gbrowserDarkModeApplied === 'device-emulation') {
                    window.__gbrowserDarkModeApplied = false;
                }
                
                // Remove touch cursor styles
                const touchStyle = document.getElementById('gbrowser-touch-cursor');
                if (touchStyle) {
                    touchStyle.remove();
                }
                
                // Remove or reset viewport meta tag
                const viewport = document.querySelector('meta[name="viewport"]');
                if (viewport) {
                    // Reset to a standard desktop viewport
                    viewport.content = 'width=device-width, initial-scale=1.0';
                }
                
                // Force recalculation of styles
                document.documentElement.style.display = 'none';
                document.documentElement.offsetHeight; // Force reflow
                document.documentElement.style.display = '';
                
                // Dispatch resize event
                window.dispatchEvent(new Event('resize'));
            })();
        """.trimIndent()

    try {
      browser.executeJavaScript(js, "", INLINE_SCRIPT_LINE)
      LOG.info("GBrowserDeviceEmulationUtil: Device emulation reset successfully")
    } catch (e: Exception) {
      LOG.error("GBrowserDeviceEmulationUtil: Failed to reset device emulation", e)
    }
  }
}