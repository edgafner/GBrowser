package com.github.gbrowser

import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.intellij.configurationStore.serialize
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * <application>
 *   <component name="GBrowserService"><![CDATA[{
 *   "isReloadTabsOnStartup": true,
 *   "historyDeleteOption": {
 *     "hours": 0,
 *     "displayText": "Delete on close IDE"
 *   },
 *   "bookmarks": [
 *     {
 *       "url": "https://plugins.jetbrains.com/plugin/14458-gbrowser",
 *       "name": "Dorkag"
 *     },
 *     {
 *       "url": "https://www.google.com/",
 *       "name": "Google"
 *     }
 *   ]
 * }]]></component>
 * </application>
 */
@TestApplication
@RunInEdt(writeIntent = true)
class GBrowserServiceTest {

  private lateinit var service: GBrowserService
  private lateinit var project: Project

  @BeforeEach
  fun setup() {
    project = ProjectManager.getInstance().defaultProject
    service = project.service<GBrowserService>()
    service.loadState(GBrowserService.SettingsState())
  }

  @Test
  fun `test default home page state serialization`() = runTest {
    service.defaultUrl = "https://www.google.com"
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "defaultHomePage": "https://www.google.com"
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test hideIdLabel state serialization`() = runTest {
    service.hideIdLabel = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "hideIdLabel": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test reload tabs on startup state serialization`() = runTest {
    service.reloadTabOnStartup = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isReloadTabsOnStartup": true
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test history delete option state serialization`() = runTest {
    service.historyItemsToKeep = 10
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "historyItemsToKeep": 10
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test bookmarks state serialization`() = runTest {
    service.bookmarks = mutableSetOf(GBrowserBookmark("https://plugins.jetbrains.com/plugin/14458-gbrowser", "Dorkag"), GBrowserBookmark("https://www.google.com/", "Google"))
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "bookmarks": [
    {
      "url": "https://plugins.jetbrains.com/plugin/14458-gbrowser",
      "name": "Dorkag"
    },
    {
      "url": "https://www.google.com/",
      "name": "Google"
    }
  ]
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test showBookMarksInToolbar state serialization`() = runTest {
    service.showBookMarksInToolbar = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "showBookMarksInToolbar": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isHistoryEnabled state serialization`() = runTest {
    service.isHistoryEnabled = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isHistoryEnabled": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isSuggestionSearchEnabled state serialization`() = runTest {
    service.isSuggestionSearchEnabled = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isSuggestionSearchEnabled": true
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isSuggestionSearchHighlighted state serialization`() = runTest {
    service.isSuggestionSearchHighlighted = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isSuggestionSearchHighlighted": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isHostHighlight state serialization`() = runTest {
    service.isHostHighlight = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isHostHighlight": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isUnSelectedTabIconTransparent state serialization`() = runTest {
    service.isUnSelectedTabIconTransparent = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isUnSelectedTabIconTransparent": true
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isFavIconEnabled state serialization`() = runTest {
    service.isFavIconEnabled = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isFavIconEnabled": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isTabIconVisible state serialization`() = runTest {
    service.isTabIconVisible = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isTabIconVisible": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isDebugEnabled state serialization`() = runTest {
    service.isDebugEnabled = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isDebugEnabled": true
}]]></state>""".trimIndent(), xml
    )
  }


  @Test
  fun `test isProtocolHidden state serialization`() = runTest {
    service.isProtocolHidden = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isProtocolHidden": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test navigateInNewTab state serialization`() = runTest {
    service.navigateInNewTab = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "navigateInNewTab": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test isDragAndDropEnabled state serialization`() = runTest {
    service.isDragAndDropEnabled = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "isDragAndDropEnabled": false
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test debugPort property and registry value`() = runTest {
    // The debugPort property is special because it's also stored in the registry
    // and not included in the serialized state
    val originalPort = service.debugPort
    try {
      service.debugPort = 1234

      // Check that the property value is set correctly
      Assertions.assertEquals(1234, service.debugPort)

      // Check that the registry value is set correctly
      Assertions.assertEquals(1234, com.github.gbrowser.util.GBrowserUtil.getJCEFDebugPort())
    } finally {
      // Restore the original value
      service.debugPort = originalPort
    }
  }

  @Test
  fun `test requestHeaders state serialization`() = runTest {
    val header = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value", 
      "test-name", 
      true, 
      "test-regex"
    )
    service.requestHeaders = mutableSetOf(header)
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "requestHeaders": [
    {
      "value": "test-value",
      "name": "test-name",
      "overwrite": true,
      "uriRegex": "test-regex"
    }
  ]
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test history state serialization`() = runTest {
    val historyItem = com.github.gbrowser.settings.dao.GBrowserHistory("Test Page", "https://example.com")
    service.history = linkedSetOf(historyItem)
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals(
      """<state><![CDATA[{
  "history": [
    {
      "name": "Test Page",
      "url": "https://example.com"
    }
  ]
}]]></state>""".trimIndent(), xml
    )
  }

  @Test
  fun `test addHistory method`() = runTest {
    service.history = linkedSetOf()
    val historyItem = com.github.gbrowser.settings.dao.GBrowserHistory("Test Page", "https://example.com")
    service.addHistory(historyItem)

    Assertions.assertEquals(1, service.history.size)
    Assertions.assertTrue(service.history.contains(historyItem))
  }

  @Test
  fun `test addHistory method with existing URL but blank name`() = runTest {
    service.history = linkedSetOf()
    val historyItem1 = com.github.gbrowser.settings.dao.GBrowserHistory("", "https://example.com")
    service.addHistory(historyItem1)

    val historyItem2 = com.github.gbrowser.settings.dao.GBrowserHistory("Test Page", "https://example.com")
    service.addHistory(historyItem2)

    Assertions.assertEquals(1, service.history.size)

    // Since GBrowserHistory.equals() only compares URLs, we can't use contains() to check
    // which specific item is in the set. Instead, we'll check that the item in the set
    // has the expected name.
    val historyItem = service.history.first()
    Assertions.assertEquals("https://example.com", historyItem.url)
    Assertions.assertEquals("Test Page", historyItem.name)
  }

  @Test
  fun `test addHistory method with limit`() = runTest {
    service.history = linkedSetOf()
    service.historyItemsToKeep = 2

    val historyItem1 = com.github.gbrowser.settings.dao.GBrowserHistory("Page 1", "https://example1.com")
    val historyItem2 = com.github.gbrowser.settings.dao.GBrowserHistory("Page 2", "https://example2.com")
    val historyItem3 = com.github.gbrowser.settings.dao.GBrowserHistory("Page 3", "https://example3.com")

    service.addHistory(historyItem1)
    service.addHistory(historyItem2)
    service.addHistory(historyItem3)

    Assertions.assertEquals(2, service.history.size)
    Assertions.assertFalse(service.history.contains(historyItem1))
    Assertions.assertTrue(service.history.contains(historyItem2))
    Assertions.assertTrue(service.history.contains(historyItem3))
  }

  @Test
  fun `test removeHistory method`() = runTest {
    val historyItem = com.github.gbrowser.settings.dao.GBrowserHistory("Test Page", "https://example.com")
    service.history = linkedSetOf(historyItem)

    service.removeHistory()

    Assertions.assertEquals(0, service.history.size)
  }

  @Test
  fun `test addBookmarks with set method`() = runTest {
    service.bookmarks = mutableSetOf()
    val bookmark1 = GBrowserBookmark("https://example1.com", "Example 1")
    val bookmark2 = GBrowserBookmark("https://example2.com", "Example 2")

    service.addBookmarks(mutableSetOf(bookmark1, bookmark2))

    Assertions.assertEquals(2, service.bookmarks.size)
    Assertions.assertTrue(service.bookmarks.contains(bookmark1))
    Assertions.assertTrue(service.bookmarks.contains(bookmark2))
  }

  @Test
  fun `test addBookmarks with single bookmark method`() = runTest {
    service.bookmarks = mutableSetOf()
    val bookmark = GBrowserBookmark("https://example.com", "Example")

    service.addBookmarks(bookmark)

    Assertions.assertEquals(1, service.bookmarks.size)
    Assertions.assertTrue(service.bookmarks.contains(bookmark))
  }

  @Test
  fun `test addBookmarks with duplicate bookmark`() = runTest {
    service.bookmarks = mutableSetOf()
    val bookmark = GBrowserBookmark("https://example.com", "Example")

    service.addBookmarks(bookmark)
    service.addBookmarks(bookmark)

    Assertions.assertEquals(1, service.bookmarks.size)
  }

  @Test
  fun `test removeBookmark method`() = runTest {
    val bookmark = GBrowserBookmark("https://example.com", "Example")
    service.bookmarks = mutableSetOf(bookmark)

    service.removeBookmark(bookmark)

    Assertions.assertEquals(0, service.bookmarks.size)
  }

  @Test
  fun `test removeBookmarks method`() = runTest {
    val bookmark = GBrowserBookmark("https://example.com", "Example")
    service.bookmarks = mutableSetOf(bookmark)

    service.removeBookmarks()

    Assertions.assertEquals(0, service.bookmarks.size)
  }

  @Test
  fun `test existBookmark method with existing bookmark`() = runTest {
    val bookmark = GBrowserBookmark("https://example.com", "Example")
    service.bookmarks = mutableSetOf(bookmark)

    val result = service.existBookmark("https://example.com")

    Assertions.assertTrue(result)
  }

  @Test
  fun `test existBookmark method with non-existing bookmark`() = runTest {
    service.bookmarks = mutableSetOf()

    val result = service.existBookmark("https://example.com")

    Assertions.assertFalse(result)
  }

  @Test
  fun `test addRequestHeader with single header method`() = runTest {
    service.requestHeaders = mutableSetOf()
    val header = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value", 
      "test-name", 
      true, 
      "test-regex"
    )

    service.addRequestHeader(header)

    Assertions.assertEquals(1, service.requestHeaders.size)
    Assertions.assertTrue(service.requestHeaders.contains(header))
  }

  @Test
  fun `test addRequestHeader with list method`() = runTest {
    service.requestHeaders = mutableSetOf()
    val header1 = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value-1", 
      "test-name-1", 
      true, 
      "test-regex-1"
    )
    val header2 = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value-2", 
      "test-name-2", 
      false, 
      "test-regex-2"
    )

    service.addRequestHeader(listOf(header1, header2))

    Assertions.assertEquals(2, service.requestHeaders.size)
    Assertions.assertTrue(service.requestHeaders.contains(header1))
    Assertions.assertTrue(service.requestHeaders.contains(header2))
  }

  @Test
  fun `test removeRequestHeader with single header method`() = runTest {
    val header = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value", 
      "test-name", 
      true, 
      "test-regex"
    )
    service.requestHeaders = mutableSetOf(header)

    service.removeRequestHeader(header)

    Assertions.assertEquals(0, service.requestHeaders.size)
  }

  @Test
  fun `test removeRequestHeader with list method`() = runTest {
    val header1 = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value-1", 
      "test-name-1", 
      true, 
      "test-regex-1"
    )
    val header2 = com.github.gbrowser.settings.request_header.GBrowserRequestHeader(
      "test-value-2", 
      "test-name-2", 
      false, 
      "test-regex-2"
    )
    service.requestHeaders = mutableSetOf(header1, header2)

    service.removeRequestHeader(listOf(header1))

    Assertions.assertEquals(1, service.requestHeaders.size)
    Assertions.assertFalse(service.requestHeaders.contains(header1))
    Assertions.assertTrue(service.requestHeaders.contains(header2))
  }
}
