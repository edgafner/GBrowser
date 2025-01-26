package com.github.gbrowser


import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.intellij.configurationStore.serialize
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

  @BeforeEach
  fun setup() {
    service = GBrowserService.instance()
    service.loadState(GBrowserService.SettingsState())  // Reset to default state
  }

  @Test
  fun `test default home page state serialization`() = runTest {
    service.defaultUrl = "https://www.google.com"
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "defaultHomePage": "https://www.google.com"
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test hideIdLabel state serialization`() = runTest {
    service.hideIdLabel = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "hideIdLabel": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test reload tabs on startup state serialization`() = runTest {
    service.reloadTabOnStartup = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isReloadTabsOnStartup": true
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test history delete option state serialization`() = runTest {
    service.historyItemsToKeep = 10
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "historyItemsToKeep": 10
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test bookmarks state serialization`() = runTest {
    service.bookmarks =
      mutableSetOf(GBrowserBookmark("https://plugins.jetbrains.com/plugin/14458-gbrowser", "Dorkag"), GBrowserBookmark("https://www.google.com/", "Google"))
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
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
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test showBookMarksInToolbar state serialization`() = runTest {
    service.showBookMarksInToolbar = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "showBookMarksInToolbar": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isHistoryEnabled state serialization`() = runTest {
    service.isHistoryEnabled = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isHistoryEnabled": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isSuggestionSearchEnabled state serialization`() = runTest {
    service.isSuggestionSearchEnabled = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isSuggestionSearchEnabled": true
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isSuggestionSearchHighlighted state serialization`() = runTest {
    service.isSuggestionSearchHighlighted = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isSuggestionSearchHighlighted": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isHostHighlight state serialization`() = runTest {
    service.isHostHighlight = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isHostHighlight": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isUnSelectedTabIconTransparent state serialization`() = runTest {
    service.isUnSelectedTabIconTransparent = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isUnSelectedTabIconTransparent": true
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isFavIconEnabled state serialization`() = runTest {
    service.isFavIconEnabled = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isFavIconEnabled": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isTabIconVisible state serialization`() = runTest {
    service.isTabIconVisible = false
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isTabIconVisible": false
}]]></state>""".trimIndent(), xml)
  }

  @Test
  fun `test isDebugEnabled state serialization`() = runTest {
    service.isDebugEnabled = true
    val state = service.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "isDebugEnabled": true
}]]></state>""".trimIndent(), xml)
  }


  //@Test
  //fun `test state deserialization`() = runTest {
  //  val service = ApplicationManager.getApplication().getService(GBrowserService::class.java)
  //  val xmlString = "<state>...</state>" // Replace with actual XML content
  //
  //  val deserializedState = service.deserializeState(xmlString)
  //  Assertions.assertNotNull(deserializedState, "Deserialized state should not be null")
  //}
  //
  //@Test
  //fun `test history management`() = runTest {
  //  val service = ApplicationManager.getApplication().getService(GBrowserService::class.java)
  //  val historyItem = GBrowserHistory("https://example.com", "Example")
  //
  //  service.addHistory(historyItem)
  //  Assertions.assertTrue(service.history.contains(historyItem), "History should contain added item")
  //
  //  service.removeHistory()
  //  Assertions.assertTrue(service.history.isEmpty(), "History should be empty after removal")
  //}
  //
  //@Test
  //fun `test bookmark management`() = runTest {
  //  val service = ApplicationManager.getApplication().getService(GBrowserService::class.java)
  //  val bookmark = GBrowserBookmark("https://example.com", "Example")
  //
  //  service.addBookmarks(mutableSetOf(bookmark))
  //  Assertions.assertTrue(service.bookmarks.contains(bookmark), "Bookmarks should contain added item")
  //
  //  service.removeBookmarks(bookmark)
  //  Assertions.assertFalse(service.bookmarks.contains(bookmark), "Bookmarks should not contain removed item")
  //}

  // Additional tests for other methods and properties as needed
}
