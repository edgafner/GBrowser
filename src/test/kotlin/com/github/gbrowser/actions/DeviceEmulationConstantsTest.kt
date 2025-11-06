package com.github.gbrowser.actions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeviceEmulationConstantsTest {

    @Test
    fun `test UI layout constants are within reasonable bounds`() {
        assertTrue(DeviceEmulationConstants.DEVICE_FRAME_PADDING > 0, "Frame padding should be positive")
        assertEquals(40, DeviceEmulationConstants.DEVICE_FRAME_PADDING)
        assertEquals(20, DeviceEmulationConstants.DEVICE_FRAME_PADDING_HALF)
        assertEquals(DeviceEmulationConstants.DEVICE_FRAME_PADDING / 2, DeviceEmulationConstants.DEVICE_FRAME_PADDING_HALF)

        assertTrue(DeviceEmulationConstants.SCALE_PADDING_FACTOR > 0.0 && DeviceEmulationConstants.SCALE_PADDING_FACTOR <= 1.0,
                   "Scale padding factor should be between 0 and 1")
        assertEquals(0.9, DeviceEmulationConstants.SCALE_PADDING_FACTOR, 0.001)

        assertTrue(DeviceEmulationConstants.DEVICE_FRAME_INNER_PADDING >= 0)
        assertTrue(DeviceEmulationConstants.DEFAULT_RESPONSIVE_WIDTH > 0)
        assertTrue(DeviceEmulationConstants.DEFAULT_RESPONSIVE_HEIGHT > 0)
    }

    @Test
    fun `test spinner constraints are valid`() {
        assertTrue(DeviceEmulationConstants.MIN_DEVICE_DIMENSION > 0, "Min dimension should be positive")
        assertTrue(DeviceEmulationConstants.MAX_DEVICE_DIMENSION > DeviceEmulationConstants.MIN_DEVICE_DIMENSION,
                   "Max dimension should be greater than min")
        assertTrue(DeviceEmulationConstants.DIMENSION_STEP > 0, "Step should be positive")

        assertEquals(50, DeviceEmulationConstants.MIN_DEVICE_DIMENSION)
        assertEquals(9999, DeviceEmulationConstants.MAX_DEVICE_DIMENSION)
        assertEquals(1, DeviceEmulationConstants.DIMENSION_STEP)
    }

    @Test
    fun `test zoom constants`() {
        assertTrue(DeviceEmulationConstants.ZOOM_FACTOR > 1.0, "Zoom factor should be greater than 1")
        assertEquals(1.2, DeviceEmulationConstants.ZOOM_FACTOR, 0.001)
        assertEquals("100%", DeviceEmulationConstants.DEFAULT_ZOOM_PERCENTAGE)
    }

    @Test
    fun `test component dimensions are positive`() {
        assertTrue(DeviceEmulationConstants.SPINNER_WIDTH > 0)
        assertTrue(DeviceEmulationConstants.ROTATE_BUTTON_WIDTH > 0)
        assertTrue(DeviceEmulationConstants.ROTATE_BUTTON_HEIGHT > 0)
        assertTrue(DeviceEmulationConstants.TOOLBAR_HORIZONTAL_GAP >= 0)
        assertTrue(DeviceEmulationConstants.TOOLBAR_VERTICAL_GAP >= 0)
        assertTrue(DeviceEmulationConstants.TOOLBAR_PADDING >= 0)
        assertTrue(DeviceEmulationConstants.TOOLBAR_HORIZONTAL_PADDING >= 0)
        assertTrue(DeviceEmulationConstants.LABEL_HORIZONTAL_PADDING >= 0)
        assertTrue(DeviceEmulationConstants.HORIZONTAL_STRUT_SIZE >= 0)
    }

    @Test
    fun `test mobile user agent contains expected browser markers`() {
        val mobileUA = DeviceEmulationConstants.MOBILE_USER_AGENT_ANDROID
        assertTrue(mobileUA.contains("Mobile"), "Mobile user agent should contain 'Mobile' keyword")
        assertTrue(mobileUA.contains("Android"), "Android user agent should contain 'Android'")
        assertTrue(mobileUA.contains("Chrome"), "Should contain Chrome")
        assertTrue(mobileUA.contains("Safari"), "Should contain Safari")
    }

    @Test
    fun `test iPhone user agents are valid and consistent`() {
        val iphoneUA = DeviceEmulationConstants.USER_AGENT_IPHONE_SE
        assertTrue(iphoneUA.contains("iPhone"), "iPhone user agent should contain 'iPhone'")
        assertTrue(iphoneUA.contains("Safari"), "iPhone user agent should contain 'Safari'")
        assertTrue(iphoneUA.contains("Mobile"), "iPhone user agent should contain 'Mobile'")

        // Verify all iPhone user agents have consistent structure
        val iphoneUserAgents = listOf(
            DeviceEmulationConstants.USER_AGENT_IPHONE_SE,
            DeviceEmulationConstants.USER_AGENT_IPHONE_XR,
            DeviceEmulationConstants.USER_AGENT_IPHONE_12_PRO
        )

        iphoneUserAgents.forEach { ua ->
            assertTrue(ua.contains("iPhone") && ua.contains("Safari"), "All iPhone UAs should contain iPhone and Safari")
        }
    }

    @Test
    fun `test Android device user agents are valid`() {
        val androidDevices = listOf(
            DeviceEmulationConstants.USER_AGENT_PIXEL_7,
            DeviceEmulationConstants.USER_AGENT_SAMSUNG_S8_PLUS,
            DeviceEmulationConstants.USER_AGENT_SAMSUNG_S20_ULTRA,
            DeviceEmulationConstants.USER_AGENT_SAMSUNG_A51_71,
            DeviceEmulationConstants.USER_AGENT_GALAXY_Z_FOLD_5
        )

        androidDevices.forEach { ua ->
            assertTrue(ua.contains("Android"), "Android device user agent should contain 'Android': $ua")
            assertTrue(ua.contains("Mobile"), "Android device user agent should contain 'Mobile': $ua")
            assertTrue(ua.contains("Chrome"), "Android device user agent should contain 'Chrome': $ua")
        }
    }

    @Test
    fun `test tablet user agents are valid`() {
        val ipadUA = DeviceEmulationConstants.USER_AGENT_IPAD_MINI
        assertTrue(ipadUA.contains("iPad"), "iPad user agent should contain 'iPad'")
        assertTrue(ipadUA.contains("Safari"), "iPad user agent should contain 'Safari'")
        assertFalse(ipadUA.contains("iPhone"), "iPad user agent should not contain 'iPhone'")
    }

    @Test
    fun `test Surface device user agents are valid`() {
        val surfacePro = DeviceEmulationConstants.USER_AGENT_SURFACE_PRO_7
        assertTrue(surfacePro.contains("Windows"), "Surface Pro user agent should contain 'Windows'")
        assertTrue(surfacePro.contains("Edg"), "Surface Pro user agent should contain Edge marker")

        val surfaceDuo = DeviceEmulationConstants.USER_AGENT_SURFACE_DUO
        assertTrue(surfaceDuo.contains("Android"), "Surface Duo user agent should contain 'Android'")
        assertTrue(surfaceDuo.contains("Surface Duo"), "Surface Duo user agent should contain 'Surface Duo'")
    }

    @Test
    fun `test default browser user agent`() {
        val defaultUA = DeviceEmulationConstants.USER_AGENT_DEFAULT_BROWSER
        assertTrue(defaultUA.contains("Windows"), "Default user agent should contain Windows")
        assertTrue(defaultUA.contains("Chrome"), "Default user agent should contain Chrome")
        assertTrue(defaultUA.contains("CefSharp"), "Default user agent should contain CefSharp marker")
    }

    @Test
    fun `test modern browser user agent for anti-detection`() {
        val modernUA = DeviceEmulationConstants.USER_AGENT_MODERN_BROWSER
        assertTrue(modernUA.contains("Windows"), "Modern user agent should contain Windows")
        assertTrue(modernUA.contains("Chrome"), "Modern user agent should contain Chrome")
        assertFalse(modernUA.contains("CefSharp"), "Modern user agent should not contain automation markers")
        assertFalse(modernUA.contains("HeadlessChrome"), "Modern user agent should not contain headless marker")
    }

    @Test
    fun `test component names for testing`() {
        assertEquals("device-width-spinner", DeviceEmulationConstants.DEVICE_WIDTH_SPINNER_NAME)
        assertEquals("device-height-spinner", DeviceEmulationConstants.DEVICE_HEIGHT_SPINNER_NAME)
        assertNotEquals(DeviceEmulationConstants.DEVICE_WIDTH_SPINNER_NAME,
                       DeviceEmulationConstants.DEVICE_HEIGHT_SPINNER_NAME,
                       "Width and height spinner names should be different")
    }

    @Test
    fun `test Chrome DevTools color constants`() {
        // Dark theme colors
        assertTrue(DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BG >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_DARK_BG >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BORDER >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BG >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BORDER >= 0)

        // Light theme colors
        assertTrue(DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BG >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_LIGHT_BG >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BORDER >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BG >= 0)
        assertTrue(DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BORDER >= 0)

        // Verify colors are within valid range (0x000000 to 0xFFFFFF)
        val colors = listOf(
            DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BG,
            DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BG,
            DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_DARK_BG,
            DeviceEmulationConstants.CHROME_DEVTOOLS_TOOLBAR_LIGHT_BG,
            DeviceEmulationConstants.CHROME_DEVTOOLS_DARK_BORDER,
            DeviceEmulationConstants.CHROME_DEVTOOLS_LIGHT_BORDER,
            DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BG,
            DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BG,
            DeviceEmulationConstants.CHROME_DEVICE_FRAME_DARK_BORDER,
            DeviceEmulationConstants.CHROME_DEVICE_FRAME_LIGHT_BORDER
        )

        colors.forEach { color ->
            assertTrue(color in 0..0xFFFFFF, "Color value should be within valid RGB range: 0x${color.toString(16)}")
        }
    }

    @Test
    fun `test timing constants`() {
        assertTrue(DeviceEmulationConstants.THEME_UPDATE_DELAY_MS > 0, "Theme update delay should be positive")
        assertEquals(100, DeviceEmulationConstants.THEME_UPDATE_DELAY_MS)
    }

    @Test
    fun `test Nest Hub user agents`() {
        val nestHub = DeviceEmulationConstants.USER_AGENT_NEST_HUB
        assertTrue(nestHub.contains("CrKey"), "Nest Hub user agent should contain CrKey")
        assertTrue(nestHub.contains("Linux"), "Nest Hub user agent should contain Linux")

        val nestHubMax = DeviceEmulationConstants.USER_AGENT_NEST_HUB_MAX
        assertTrue(nestHubMax.contains("CrKey"), "Nest Hub Max user agent should contain CrKey")
        assertTrue(nestHubMax.contains("Linux"), "Nest Hub Max user agent should contain Linux")
    }
}
