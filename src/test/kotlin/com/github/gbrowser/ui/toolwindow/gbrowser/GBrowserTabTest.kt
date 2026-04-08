package com.github.gbrowser.ui.toolwindow.gbrowser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class GBrowserTabTest {

  @Test
  fun `equals returns true for same url regardless of name or date`() {
    val tab1 = GBrowserTab("https://example.com", "Tab 1", Date(1000))
    val tab2 = GBrowserTab("https://example.com", "Different Name", Date(2000))
    assertEquals(tab1, tab2)
  }

  @Test
  fun `equals returns false for different urls`() {
    val tab1 = GBrowserTab("https://example.com", "Tab", Date())
    val tab2 = GBrowserTab("https://other.com", "Tab", Date())
    assertNotEquals(tab1, tab2)
  }

  @Test
  fun `equals returns true for same instance`() {
    val tab = GBrowserTab("https://example.com", "Tab", Date())
    assertEquals(tab, tab)
  }

  @Test
  fun `equals returns false for different type`() {
    val tab = GBrowserTab("https://example.com", "Tab", Date())
    assertNotEquals(tab, "not a tab")
  }

  @Test
  fun `hashCode is consistent for tabs with same url`() {
    val tab1 = GBrowserTab("https://example.com", "Name 1", Date(1000))
    val tab2 = GBrowserTab("https://example.com", "Name 2", Date(2000))
    assertEquals(tab1.hashCode(), tab2.hashCode())
  }

  @Test
  fun `tabs in set deduplicate by url`() {
    val tabs = mutableSetOf(
      GBrowserTab("https://example.com", "First", Date(1000)),
      GBrowserTab("https://example.com", "Second", Date(2000)),
      GBrowserTab("https://other.com", "Third", Date(3000))
    )
    assertEquals(2, tabs.size)
  }

  @Test
  fun `default constructor creates tab with empty values`() {
    val tab = GBrowserTab(createdAt = Date())
    assertEquals("", tab.url)
    assertEquals("", tab.name)
  }

  @Test
  fun `properties are accessible`() {
    val date = Date()
    val tab = GBrowserTab("https://example.com", "My Tab", date)
    assertEquals("https://example.com", tab.url)
    assertEquals("My Tab", tab.name)
    assertEquals(date, tab.createdAt)
  }
}
