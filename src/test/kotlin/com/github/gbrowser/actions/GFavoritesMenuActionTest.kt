package com.github.gbrowser.actions

import com.github.gbrowser.services.GivServiceSettings
import com.github.gbrowser.settings.FavoritesWeb
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

class GFavoritesMenuActionTest : BehaviorSpec(), KoinTest {

  // Declare mockFavorites here, so it can be accessed in any nested scope
  private val mockFavorites = mutableListOf(FavoritesWeb("https://github.com/sponsors/edgafner"))

  init {

    // Set up Koin for this test
    beforeSpec {
      startKoin { }
      val app = mockk<Application>(relaxed = true)
      val settings = mockk<GivServiceSettings>(relaxed = true)
      val actionManager = mockk<ActionManager>(relaxed = true)

      mockkStatic(ApplicationManager::class)
      every { ApplicationManager.getApplication() } returns app
      every { app.getService(GivServiceSettings::class.java) } returns settings

      // Mocking the response for ActionManager::class.java
      every { app.getService(ActionManager::class.java) } returns actionManager

      mockkStatic(GivServiceSettings::class)
      every { GivServiceSettings.instance() } returns settings

      every { settings.getFavorites() } returns mockFavorites

      // Mock ActionManager.getInstance() to return the mock ActionManager
      mockkStatic(ActionManager::class)
      every { ActionManager.getInstance() } returns actionManager
    }

    afterSpec {
      unmockkAll()
      stopKoin()
    }

    given("GFavoritesMenuAction is initialized") {
      val mockJBCefBrowser = mockk<JBCefBrowser>(relaxed = true)
      val gFavoritesMenuAction = GFavoritesMenuAction(mockJBCefBrowser)

      `when`("updateView function is called") {
        gFavoritesMenuAction.updateView()

        then("GFavoritesMenuAction should have the correct actions added") {
          gFavoritesMenuAction.childrenCount shouldBe 1
          gFavoritesMenuAction.childActionsOrStubs[0].templateText shouldBe "https://github.com/sponsors/edgafner"
        }
      }
    }
  }
}



