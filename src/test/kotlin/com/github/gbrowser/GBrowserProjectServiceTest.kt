package com.github.gbrowser

import com.github.gbrowser.services.GBrowserProjectService
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserTab
import com.intellij.configurationStore.serialize
import com.intellij.openapi.util.JDOMUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for GBrowserProjectService
 */
class GBrowserProjectServiceTest {

  private lateinit var service: GBrowserProjectService

  @BeforeEach
  fun setup() {
    // Create a simple instance without platform initialization
    service = GBrowserProjectService()
    service.loadState(GBrowserProjectService.ProjectSettingsState())
  }


  @Test
  fun `test tabs state serialization`() {
    val tab = GBrowserTab("https://example.com", "Example", Date())
    service.tabs = mutableSetOf(tab)
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    // The date will be serialized in ISO format, so we need to check that the XML contains the expected parts
    Assertions.assertTrue(xml.contains("\"url\": \"https://example.com\""))
    Assertions.assertTrue(xml.contains("\"name\": \"Example\""))
    Assertions.assertTrue(xml.contains("\"createdAt\""))
  }

  @Test
  fun `test addTab method`() {
    service.tabs = mutableSetOf()
    val tab = GBrowserTab("https://example.com", "Example", Date())

    service.addTab(tab)

    Assertions.assertEquals(1, service.tabs.size)
    Assertions.assertTrue(service.tabs.contains(tab))
  }

  @Test
  fun `test addTab method with duplicate URL`() {
    service.tabs = mutableSetOf()
    val date1 = Date()
    val date2 = Date(date1.time + 1000) // 1 second later

    val tab1 = GBrowserTab("https://example.com", "Example 1", date1)
    val tab2 = GBrowserTab("https://example.com", "Example 2", date2)

    service.addTab(tab1)
    service.addTab(tab2)

    // Since tabs are compared by URL only (as per equals method in GBrowserTab),
    // only one tab should be in the set
    Assertions.assertEquals(1, service.tabs.size)

    // The set should contain tab2 since it was added last
    val tabInSet = service.tabs.first()
    Assertions.assertEquals("https://example.com", tabInSet.url)

    // Note: We can't reliably check which name is in the set since sets don't guarantee
    // replacement order when equals() returns true but the objects are different
  }

  @Test
  fun `test addTabs method`() {
    service.tabs = mutableSetOf()
    val tab1 = GBrowserTab("https://example1.com", "Example 1", Date())
    val tab2 = GBrowserTab("https://example2.com", "Example 2", Date())

    service.addTabs(listOf(tab1, tab2))

    Assertions.assertEquals(2, service.tabs.size)
    Assertions.assertTrue(service.tabs.contains(tab1))
    Assertions.assertTrue(service.tabs.contains(tab2))
  }

  @Test
  fun `test addTabs method with duplicate URLs`() {
    service.tabs = mutableSetOf()
    val tab1 = GBrowserTab("https://example1.com", "Example 1", Date())
    val tab2 = GBrowserTab("https://example1.com", "Example 2", Date())
    val tab3 = GBrowserTab("https://example2.com", "Example 3", Date())

    service.addTabs(listOf(tab1, tab2, tab3))

    // Since tabs are compared by URL only, only two tabs should be in the set
    Assertions.assertEquals(2, service.tabs.size)

    // Check that both unique URLs are in the set
    val urls = service.tabs.map { it.url }.toSet()
    Assertions.assertTrue(urls.contains("https://example1.com"))
    Assertions.assertTrue(urls.contains("https://example2.com"))
  }

  @Test
  fun `test removeTab method`() {
    val tab = GBrowserTab("https://example.com", "Example", Date())
    service.tabs = mutableSetOf(tab)

    service.removeTab(tab)

    Assertions.assertEquals(0, service.tabs.size)
  }

  @Test
  fun `test removeTab method with non-existing tab`() {
    val tab1 = GBrowserTab("https://example1.com", "Example 1", Date())
    val tab2 = GBrowserTab("https://example2.com", "Example 2", Date())

    service.tabs = mutableSetOf(tab1)
    service.removeTab(tab2)

    Assertions.assertEquals(1, service.tabs.size)
    Assertions.assertTrue(service.tabs.contains(tab1))
  }
}
