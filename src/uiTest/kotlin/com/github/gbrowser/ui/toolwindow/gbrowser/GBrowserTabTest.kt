package com.github.gbrowser.ui.toolwindow.gbrowser

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class GBrowserTabTest {

  @Test
  fun `test equals with same URL but different names and dates`() {
    val date1 = Date()
    val date2 = Date(date1.time + 1000) // 1 second later

    val tab1 = GBrowserTab("https://example.com", "Tab 1", date1)
    val tab2 = GBrowserTab("https://example.com", "Tab 2", date2)

    Assertions.assertEquals(tab1, tab2, "Tabs with the same URL should be equal")
    Assertions.assertEquals(tab1.hashCode(), tab2.hashCode(), "Hash codes should be equal for equal objects")
  }

  @Test
  fun `test equals with different URLs`() {
    val date = Date()

    val tab1 = GBrowserTab("https://example1.com", "Tab 1", date)
    val tab2 = GBrowserTab("https://example2.com", "Tab 1", date)

    Assertions.assertNotEquals(tab1, tab2, "Tabs with different URLs should not be equal")
    Assertions.assertNotEquals(tab1.hashCode(), tab2.hashCode(), "Hash codes should not be equal for unequal objects")
  }

  @Test
  fun `test equals with null and different type`() {
    val tab = GBrowserTab("https://example.com", "Tab 1", Date())

    Assertions.assertNotEquals(tab, null, "Tab should not be equal to null")
  }

  @Test
  fun `test equals with same object reference`() {
    val tab = GBrowserTab("https://example.com", "Tab 1", Date())

    Assertions.assertEquals(tab, tab, "Tab should be equal to itself")
  }

  @Test
  fun `test constructor with default values`() {
    val date = Date()
    val tab = GBrowserTab(createdAt = date)

    Assertions.assertEquals("", tab.url, "The default URL should be an empty string")
    Assertions.assertEquals("", tab.name, "The default name should be an empty string")
    Assertions.assertEquals(date, tab.createdAt, "Creation date should match the provided date")
  }

  @Test
  fun `test serialVersionUID constant`() { // Just verify that the constant exists and has the expected value
    Assertions.assertEquals(4423235970041806118L, GBrowserTab.Companion.serialVersionUID)
  }
}