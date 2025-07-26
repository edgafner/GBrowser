package com.github.gbrowser.settings.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GBrowserThemeTest {

  @Test
  fun `test theme enum values`() {
    val themes = GBrowserTheme.entries.toTypedArray()
    assertEquals(3, themes.size)
    assertTrue(themes.contains(GBrowserTheme.FOLLOW_IDE))
    assertTrue(themes.contains(GBrowserTheme.LIGHT))
    assertTrue(themes.contains(GBrowserTheme.DARK))
  }

  @Test
  fun `test theme display names`() {
    assertEquals("Follow IDE", GBrowserTheme.FOLLOW_IDE.displayName)
    assertEquals("Light", GBrowserTheme.LIGHT.displayName)
    assertEquals("Dark", GBrowserTheme.DARK.displayName)
  }

  @Test
  fun `test fromString with valid values`() {
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("FOLLOW_IDE"))
    assertEquals(GBrowserTheme.LIGHT, GBrowserTheme.fromString("LIGHT"))
    assertEquals(GBrowserTheme.DARK, GBrowserTheme.fromString("DARK"))
  }

  @Test
  fun `test fromString with null returns default`() {
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString(null))
  }

  @Test
  fun `test fromString with invalid value returns default`() {
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("INVALID"))
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString(""))
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("follow_ide"))
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("Light"))
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("dark"))
  }

  @Test
  fun `test fromString is case sensitive`() {
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("follow_ide"))
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("FOLLOW_ide"))
    assertEquals(GBrowserTheme.FOLLOW_IDE, GBrowserTheme.fromString("Follow_IDE"))
  }

  @Test
  fun `test theme name property`() {
    assertEquals("FOLLOW_IDE", GBrowserTheme.FOLLOW_IDE.name)
    assertEquals("LIGHT", GBrowserTheme.LIGHT.name)
    assertEquals("DARK", GBrowserTheme.DARK.name)
  }

  @Test
  fun `test ordinal values`() {
    assertEquals(0, GBrowserTheme.FOLLOW_IDE.ordinal)
    assertEquals(1, GBrowserTheme.LIGHT.ordinal)
    assertEquals(2, GBrowserTheme.DARK.ordinal)
  }
}