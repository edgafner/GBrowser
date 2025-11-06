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
