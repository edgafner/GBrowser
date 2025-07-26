package com.github.gbrowser.actions

/**
 * Constants used for device emulation functionality.
 * Centralizes magic numbers and strings for better maintainability.
 */
object DeviceEmulationConstants {
  // UI Layout Constants
  const val DEVICE_FRAME_PADDING = 40 // Total padding (20px on each side)
  const val DEVICE_FRAME_PADDING_HALF = 20 // Padding on one side
  const val SCALE_PADDING_FACTOR = 0.9 // 90% scale to ensure padding
  const val DEVICE_FRAME_INNER_PADDING = 2 // Inner padding for device frame border

  // Spinner Constraints
  const val MIN_DEVICE_DIMENSION = 50
  const val MAX_DEVICE_DIMENSION = 9999
  const val DIMENSION_STEP = 1

  // Zoom Constants
  const val ZOOM_FACTOR = 1.2
  const val DEFAULT_ZOOM_PERCENTAGE = "100%"

  // Component Dimensions
  const val SPINNER_WIDTH = 80
  const val ROTATE_BUTTON_WIDTH = 30
  const val ROTATE_BUTTON_HEIGHT = 26
  const val TOOLBAR_HORIZONTAL_GAP = 5
  const val TOOLBAR_VERTICAL_GAP = 2
  const val TOOLBAR_PADDING = 5
  const val TOOLBAR_HORIZONTAL_PADDING = 10
  const val LABEL_HORIZONTAL_PADDING = 3
  const val HORIZONTAL_STRUT_SIZE = 10

  // User Agent Strings (Updated to latest versions)
  const val MOBILE_USER_AGENT_ANDROID = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

  // Device-specific User Agents
  const val USER_AGENT_IPHONE_SE = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1"
  const val USER_AGENT_IPHONE_XR = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"
  const val USER_AGENT_IPHONE_12_PRO = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"
  const val USER_AGENT_PIXEL_7 = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
  const val USER_AGENT_SAMSUNG_S8_PLUS = "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
  const val USER_AGENT_SAMSUNG_S20_ULTRA = "Mozilla/5.0 (Linux; Android 10; SM-G988B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
  const val USER_AGENT_SAMSUNG_A51_71 = "Mozilla/5.0 (Linux; Android 11; SM-A515F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
  const val USER_AGENT_GALAXY_Z_FOLD_5 = "Mozilla/5.0 (Linux; Android 13; SM-F946B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
  const val USER_AGENT_IPAD_MINI = "Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1"
  const val USER_AGENT_SURFACE_PRO_7 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36 Edg/116.0.0.0"
  const val USER_AGENT_SURFACE_DUO = "Mozilla/5.0 (Linux; Android 11; Surface Duo) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
  const val USER_AGENT_ASUS_ZENBOOK_FOLD = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36"
  const val USER_AGENT_NEST_HUB = "Mozilla/5.0 (X11; Linux armv7l) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36 CrKey/1.56.500000"
  const val USER_AGENT_NEST_HUB_MAX = "Mozilla/5.0 (X11; Linux aarch64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36 CrKey/1.56.500000"

  // Default Browser User Agent
  const val USER_AGENT_DEFAULT_BROWSER =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36 /CefSharp Browser 90.0"

  // Component Names for Testing
  const val DEVICE_WIDTH_SPINNER_NAME = "device-width-spinner"
  const val DEVICE_HEIGHT_SPINNER_NAME = "device-height-spinner"

  // Chrome DevTools Colors
  const val CHROME_DEVTOOLS_DARK_BG = 0x202124
  const val CHROME_DEVTOOLS_LIGHT_BG = 0xF3F3F3
  const val CHROME_DEVTOOLS_TOOLBAR_DARK_BG = 0x2B2D30
  const val CHROME_DEVTOOLS_TOOLBAR_LIGHT_BG = 0xF3F3F3
  const val CHROME_DEVTOOLS_DARK_BORDER = 0x393B3F
  const val CHROME_DEVTOOLS_LIGHT_BORDER = 0xD0D0D0
  const val CHROME_DEVICE_FRAME_DARK_BG = 0x292A2D
  const val CHROME_DEVICE_FRAME_LIGHT_BG = 0xFFFFFF
  const val CHROME_DEVICE_FRAME_DARK_BORDER = 0x3C3F41
  const val CHROME_DEVICE_FRAME_LIGHT_BORDER = 0xD0D0D0

  // Timing Constants
  const val THEME_UPDATE_DELAY_MS = 100
}