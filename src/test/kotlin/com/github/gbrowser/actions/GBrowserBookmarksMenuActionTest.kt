package com.github.gbrowser.actions

import com.github.gbrowser.services.GBrowserSettings
import com.github.gbrowser.settings.GBrowserBookmarks
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.jcef.JBCefBrowser
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

class GBrowserBookmarksMenuActionTest : BehaviorSpec(), KoinTest {

  // Declare mockFavorites here, so it can be accessed in any nested scope
  private val mockBookmarks = mutableListOf(GBrowserBookmarks("https://dorkag.com/dorkag"))

  init {

    // Set up Koin for this test
    beforeSpec {
      startKoin { }
      val app = mockk<Application>(relaxed = true)
      val settings = mockk<GBrowserSettings>(relaxed = true)
      val actionManager = mockk<ActionManager>(relaxed = true)

      mockkStatic(ApplicationManager::class)
      every { ApplicationManager.getApplication() } returns app
      every { app.getService(GBrowserSettings::class.java) } returns settings

      // Mocking the response for ActionManager::class.java
      every { app.getService(ActionManager::class.java) } returns actionManager

      mockkStatic(GBrowserSettings::class)
      every { GBrowserSettings.instance() } returns settings

      every { settings.getBookmarks() } returns mockBookmarks

      // Mock ActionManager.getInstance() to return the mock ActionManager
      mockkStatic(ActionManager::class)
      every { ActionManager.getInstance() } returns actionManager
    }

    afterSpec {
      unmockkAll()
      stopKoin()
    }

    given("GBookmarksMenuAction is initialized") {
      val mockJBCefBrowser = mockk<JBCefBrowser>(relaxed = true)
      val gBookMarksMenuAction = GBookMarksMenuAction(mockJBCefBrowser)

      `when`("updateView function is called") {
        gBookMarksMenuAction.updateView()

        then("GBookmarksMenuAction should have the correct actions added") {
          gBookMarksMenuAction.childrenCount shouldBe 1
          gBookMarksMenuAction.childActionsOrStubs[0].templateText shouldBe "https://dorkag.com/dorkag"
        }
      }
    }
  }
}



