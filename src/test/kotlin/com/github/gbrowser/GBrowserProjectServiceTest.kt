package com.github.gbrowser

import com.github.gbrowser.services.GBrowserProjectService
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserTab
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class GBrowserProjectServiceTest {

  private lateinit var service: GBrowserProjectService

  @BeforeEach
  fun setup() {
    service = GBrowserProjectService()
    service.loadState(GBrowserProjectService.ProjectSettingsState())
  }

  @Test
  fun `initial state has empty tabs`() {
    assertTrue(service.tabs.isEmpty())
  }

  @Test
  fun `addTab adds a single tab`() {
    val tab = GBrowserTab("https://example.com", "Example", Date())
    service.addTab(tab)

    assertEquals(1, service.tabs.size)
    assertTrue(service.tabs.contains(tab))
  }

  @Test
  fun `addTab with duplicate url does not add second tab`() {
    val tab1 = GBrowserTab("https://example.com", "First", Date(1000))
    val tab2 = GBrowserTab("https://example.com", "Second", Date(2000))

    service.addTab(tab1)
    service.addTab(tab2)

    assertEquals(1, service.tabs.size)
  }

  @Test
  fun `addTabs adds multiple tabs`() {
    val tabs = listOf(
      GBrowserTab("https://example1.com", "Tab 1", Date()),
      GBrowserTab("https://example2.com", "Tab 2", Date())
    )
    service.addTabs(tabs)

    assertEquals(2, service.tabs.size)
  }

  @Test
  fun `removeTab removes existing tab`() {
    val tab = GBrowserTab("https://example.com", "Example", Date())
    service.addTab(tab)
    service.removeTab(tab)

    assertTrue(service.tabs.isEmpty())
  }

  @Test
  fun `removeTab with non-existing tab does nothing`() {
    val tab1 = GBrowserTab("https://example.com", "Example", Date())
    val tab2 = GBrowserTab("https://other.com", "Other", Date())
    service.addTab(tab1)
    service.removeTab(tab2)

    assertEquals(1, service.tabs.size)
  }

  @Test
  fun `setting tabs replaces all tabs`() {
    service.addTab(GBrowserTab("https://old.com", "Old", Date()))

    val newTabs = mutableSetOf(
      GBrowserTab("https://new1.com", "New 1", Date()),
      GBrowserTab("https://new2.com", "New 2", Date())
    )
    service.tabs = newTabs

    assertEquals(2, service.tabs.size)
    assertTrue(service.tabs.any { it.url == "https://new1.com" })
    assertTrue(service.tabs.any { it.url == "https://new2.com" })
  }
}
