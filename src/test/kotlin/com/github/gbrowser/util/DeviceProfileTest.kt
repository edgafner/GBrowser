package com.github.gbrowser.util

import com.github.gbrowser.actions.DeviceEmulationConstants
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DeviceProfileTest {

    @Test
    fun `test DeviceProfile creation with all parameters`() {
        val profile = DeviceProfile(
            name = "iPhone 12 Pro",
            width = 390,
            height = 844,
            deviceScaleFactor = 3.0,
            userAgent = "Mozilla/5.0...",
            isMobile = true,
            hasTouch = true
        )

        assertEquals("iPhone 12 Pro", profile.name)
        assertEquals(390, profile.width)
        assertEquals(844, profile.height)
        assertEquals(3.0, profile.deviceScaleFactor, 0.001)
        assertEquals("Mozilla/5.0...", profile.userAgent)
        assertTrue(profile.isMobile)
        assertTrue(profile.hasTouch)
    }

    @Test
    fun `test DeviceProfile with default parameters`() {
        val profile = DeviceProfile(
            name = "Test Device",
            width = 400,
            height = 600,
            deviceScaleFactor = 2.0,
            userAgent = "Test UA"
        )

        assertTrue(profile.isMobile, "Default isMobile should be true")
        assertTrue(profile.hasTouch, "Default hasTouch should be true")
    }

    @Test
    fun `test DeviceProfile with desktop configuration`() {
        val profile = DeviceProfile(
            name = "Desktop",
            width = 1920,
            height = 1080,
            deviceScaleFactor = 1.0,
            userAgent = "Desktop UA",
            isMobile = false,
            hasTouch = false
        )

        assertFalse(profile.isMobile)
        assertFalse(profile.hasTouch)
    }

    @Test
    fun `test DeviceProfile data class copy functionality`() {
        val original = DeviceProfile(
            name = "Original",
            width = 390,
            height = 844,
            deviceScaleFactor = 3.0,
            userAgent = "UA"
        )

        val copy = original.copy(name = "Modified")

        assertEquals("Modified", copy.name)
        assertEquals(390, copy.width) // Unchanged
        assertEquals(844, copy.height) // Unchanged
    }

    @Test
    fun `test DeviceProfile equals and hashCode`() {
        val profile1 = DeviceProfile(
            name = "iPhone",
            width = 390,
            height = 844,
            deviceScaleFactor = 3.0,
            userAgent = "UA"
        )

        val profile2 = DeviceProfile(
            name = "iPhone",
            width = 390,
            height = 844,
            deviceScaleFactor = 3.0,
            userAgent = "UA"
        )

        assertEquals(profile1, profile2)
        assertEquals(profile1.hashCode(), profile2.hashCode())
    }

    @Test
    fun `test DeviceProfile toString contains all properties`() {
        val profile = DeviceProfile(
            name = "Test",
            width = 100,
            height = 200,
            deviceScaleFactor = 2.0,
            userAgent = "UA"
        )

        val toString = profile.toString()
        assertTrue(toString.contains("Test"))
        assertTrue(toString.contains("100"))
        assertTrue(toString.contains("200"))
    }
}

class GBrowserDeviceEmulationUtilTest {

    @Test
    fun `test DEVICE_PROFILES map is not empty`() {
        assertFalse(GBrowserDeviceEmulationUtil.DEVICE_PROFILES.isEmpty(),
                   "Device profiles map should contain device definitions")
    }

    @Test
    fun `test all device profiles have valid dimensions`() {
        GBrowserDeviceEmulationUtil.DEVICE_PROFILES.forEach { (name, profile) ->
            assertTrue(profile.width > 0, "Device $name should have positive width")
            assertTrue(profile.height > 0, "Device $name should have positive height")
            assertTrue(profile.width >= 50, "Device $name width should be at least 50px")
            assertTrue(profile.height >= 50, "Device $name height should be at least 50px")
        }
    }

    @Test
    fun `test all device profiles have valid device scale factors`() {
        GBrowserDeviceEmulationUtil.DEVICE_PROFILES.forEach { (name, profile) ->
            assertTrue(profile.deviceScaleFactor > 0.0,
                      "Device $name should have positive device scale factor")
            assertTrue(profile.deviceScaleFactor <= 5.0,
                      "Device $name device scale factor should be reasonable (≤5.0)")
        }
    }

    @Test
    fun `test all device profiles have non-empty user agents`() {
        GBrowserDeviceEmulationUtil.DEVICE_PROFILES.forEach { (name, profile) ->
            assertFalse(profile.userAgent.isBlank(),
                       "Device $name should have a non-empty user agent")
            assertTrue(profile.userAgent.length > 10,
                      "Device $name user agent should be a reasonable length")
        }
    }

    @Test
    fun `test all device profiles have matching names`() {
        GBrowserDeviceEmulationUtil.DEVICE_PROFILES.forEach { (key, profile) ->
            assertEquals(key, profile.name,
                        "Map key should match profile name for $key")
        }
    }

    @Test
    fun `test iPhone devices exist and have valid profiles`() {
        val iphones = listOf("iPhone SE", "iPhone XR", "iPhone 12 Pro")

        iphones.forEach { deviceName ->
            val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES[deviceName]
            assertNotNull(profile, "$deviceName should exist in device profiles")
            profile?.let {
                assertTrue(it.isMobile, "$deviceName should be marked as mobile")
                assertTrue(it.hasTouch, "$deviceName should have touch support")
                assertTrue(it.userAgent.contains("iPhone"),
                          "$deviceName user agent should contain 'iPhone'")
            }
        }
    }

    @Test
    fun `test Android devices exist and have valid profiles`() {
        val androidDevices = listOf("Pixel 7", "Samsung Galaxy S8+", "Samsung Galaxy S20 Ultra")

        androidDevices.forEach { deviceName ->
            val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES[deviceName]
            assertNotNull(profile, "$deviceName should exist in device profiles")
            profile?.let {
                assertTrue(it.isMobile, "$deviceName should be marked as mobile")
                assertTrue(it.hasTouch, "$deviceName should have touch support")
                assertTrue(it.userAgent.contains("Android"),
                          "$deviceName user agent should contain 'Android'")
            }
        }
    }

    @Test
    fun `test tablet devices exist and have valid profiles`() {
        val tablets = listOf("iPad Mini", "iPad Air", "iPad Pro")

        tablets.forEach { deviceName ->
            val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES[deviceName]
            assertNotNull(profile, "$deviceName should exist in device profiles")
            profile?.let {
                assertTrue(it.width >= 768, "$deviceName should have tablet-sized width")
                assertTrue(it.userAgent.contains("iPad"),
                          "$deviceName user agent should contain 'iPad'")
            }
        }
    }

    @Test
    fun `test Surface devices exist and have valid profiles`() {
        val surfaceDevices = listOf("Surface Pro 7", "Surface Duo")

        surfaceDevices.forEach { deviceName ->
            val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES[deviceName]
            assertNotNull(profile, "$deviceName should exist in device profiles")
            assertNotNull(profile)
        }
    }

    @Test
    fun `test foldable devices exist and have valid profiles`() {
        val foldables = listOf("Galaxy Z Fold 5", "Asus Zenbook Fold")

        foldables.forEach { deviceName ->
            val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES[deviceName]
            assertNotNull(profile, "$deviceName should exist in device profiles")
        }
    }

    @Test
    fun `test smart display devices exist and have valid profiles`() {
        val smartDisplays = listOf("Nest Hub", "Nest Hub Max")

        smartDisplays.forEach { deviceName ->
            val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES[deviceName]
            assertNotNull(profile, "$deviceName should exist in device profiles")
            profile?.let {
                assertTrue(it.userAgent.contains("CrKey"),
                          "$deviceName user agent should contain 'CrKey'")
            }
        }
    }

    @Test
    fun `test device profiles have reasonable aspect ratios`() {
        GBrowserDeviceEmulationUtil.DEVICE_PROFILES.forEach { (name, profile) ->
            val aspectRatio = profile.width.toDouble() / profile.height.toDouble()
            // Most devices have aspect ratios between 0.3 and 3.0
            assertTrue(aspectRatio > 0.3,
                      "Device $name aspect ratio ($aspectRatio) should be > 0.3")
            assertTrue(aspectRatio < 3.0,
                      "Device $name aspect ratio ($aspectRatio) should be < 3.0")
        }
    }

    @Test
    fun `test specific device has expected dimensions - iPhone SE`() {
        val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["iPhone SE"]
        assertNotNull(profile)
        profile?.let {
            assertEquals(375, it.width)
            assertEquals(667, it.height)
            assertEquals(2.0, it.deviceScaleFactor, 0.001)
        }
    }

    @Test
    fun `test specific device has expected dimensions - Pixel 7`() {
        val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Pixel 7"]
        assertNotNull(profile)
        profile?.let {
            assertEquals(412, it.width)
            assertEquals(915, it.height)
            assertEquals(2.625, it.deviceScaleFactor, 0.001)
        }
    }

    @Test
    fun `test specific device has expected dimensions - iPad Mini`() {
        val profile = GBrowserDeviceEmulationUtil.DEVICE_PROFILES["iPad Mini"]
        assertNotNull(profile)
        profile?.let {
            assertEquals(768, it.width)
            assertEquals(1024, it.height)
            assertEquals(2.0, it.deviceScaleFactor, 0.001)
        }
    }

    @Test
    fun `test all device user agents match DeviceEmulationConstants`() {
        // Verify iPhone devices use correct constants
        GBrowserDeviceEmulationUtil.DEVICE_PROFILES["iPhone SE"]?.let {
            assertEquals(DeviceEmulationConstants.USER_AGENT_IPHONE_SE, it.userAgent)
        }

        GBrowserDeviceEmulationUtil.DEVICE_PROFILES["Pixel 7"]?.let {
            assertEquals(DeviceEmulationConstants.USER_AGENT_PIXEL_7, it.userAgent)
        }

        GBrowserDeviceEmulationUtil.DEVICE_PROFILES["iPad Mini"]?.let {
            assertEquals(DeviceEmulationConstants.USER_AGENT_IPAD_MINI, it.userAgent)
        }
    }

    @Test
    fun `test device profiles map size is reasonable`() {
        val size = GBrowserDeviceEmulationUtil.DEVICE_PROFILES.size
        assertTrue(size >= 15, "Should have at least 15 device profiles")
        assertTrue(size <= 50, "Should have at most 50 device profiles (sanity check)")
    }
}
