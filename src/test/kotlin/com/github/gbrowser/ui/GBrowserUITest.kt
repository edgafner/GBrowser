package com.github.gbrowser.ui

import com.github.gbrowser.ui.fixuters.GBrowserToolWindowPanel
import com.github.gbrowser.ui.fixuters.gBrowserToolWindow
import com.github.gbrowser.ui.fixuters.showGBrowserToolWindow
import com.github.gbrowser.ui.fixuters.waitForIndicatorsIgnore
import com.github.gbrowser.ui.utils.buttonByIcon
import com.github.gbrowser.ui.utils.cleanEnterTextEnter
import com.github.gbrowser.ui.utils.selectAll
import com.intellij.driver.sdk.ui.*
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitVisible
import com.intellij.driver.sdk.ui.components.common.dialogs.newProjectDialog
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.welcomeScreen
import com.intellij.driver.sdk.ui.components.elements.*
import com.intellij.driver.sdk.wait
import com.intellij.ide.starter.driver.engine.BackgroundRun
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.config.UseLatestDownloadedIdeBuild
import com.intellij.openapi.diagnostic.logger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

@Tag("ui")
@ExtendWith(UseLatestDownloadedIdeBuild::class)
class GBrowserUITest {

  companion object {
    val LOG = logger<GBrowserUITest>()
  }

  private lateinit var run: BackgroundRun
  private lateinit var tempDir: Path
  private lateinit var projectName: String

  @BeforeEach
  fun setupTest() {
    projectName = "GBrowserTest_${System.currentTimeMillis()}"

    // Create a parent directory for our project
    val baseDir = Files.createTempDirectory("gbrowser-parent")
    tempDir = baseDir.resolve(projectName)
    Files.createDirectories(tempDir)

    run = Setup.setupTestContext("GBrowserUITest").runIdeWithDriver()
  }

  @AfterEach
  fun closeIde() {
    try {
      run.closeIdeAndWait()
    } finally { // Ensure cleanup happens even if the test fails
      if (::tempDir.isInitialized && Files.exists(tempDir)) {
        tempDir.toFile().deleteRecursively()
      }
    }
  }

  @Test
  fun gBrowserToolWindow() {
    run.driver.withContext {
      welcomeScreen {
        createNewProjectButton.click()

        try {
          newProjectDialog {
            wait(1.seconds)

            chooseProjectType("Java")

            sampleCodeLabel.enabled()

            setProjectName(projectName)

            createButton.click()
          }
        } catch (e: Exception) {
          LOG.warn("Unable to create a project using newProjectDialog, trying to fall back", e)
          throw e
        }
      }

      ideFrame {
        waitForIndicatorsIgnore()
        showGBrowserToolWindow()
        wait(1.seconds)

        gBrowserToolWindow {

          actionButtonByXpath("//div[contains(@accessiblename, 'Add Tab')]").waitVisible(5.seconds).click()

          basicTab()

          browserActions()

        }

      }
    }
  }

  private fun GBrowserToolWindowPanel.basicTab() {
    gBrowserPanel { // Be more careful with drag and drop which can be flaky

      val textField = textField("//div[@class='TextFieldWithProcessing']")

      with(textField) {
        waitFound(3.seconds).doubleClick()
        selectAll()
        cleanEnterTextEnter("https://github.com")
      }

      wait(2.seconds)

      actionButtonByXpath("//div[contains(@accessiblename, 'Bookmark')]").waitVisible(5.seconds).click()

      actionButtonByXpath("//div[@accessiblename='Reload Page']").waitVisible(5.seconds).click()


      actionButtonByXpath("//div[@accessiblename='Backward']").waitVisible(5.seconds).click()
    }
  }

  private fun GBrowserToolWindowPanel.browserActions() {

    // Home menu action
    selectPopupMenuItem("Home")

    selectPopupMenuItem("Find...")
    keyboard {
      escape()
    }
    selectPopupMenuItem("Add to Bo")

    selectPopupMenuItem("Zoom Out")
    selectPopupMenuItem("Zoom In")
    selectPopupMenuItem("Close Tab")


    selectPopupMenuItem("Reload Page")
    selectPopupMenuItem("Preferences")
    presencesActions()

    gBrowserPanel {
      button {
        byTooltip("https://github.com/")
      }.shouldBe { visible() }.click()

      rightClick()
      driver.ui.ideFrame {
        popup().accessibleList().clickItem("Open DevTools", false)
        wait(1.seconds)
      }
    }

    keyboard {
      escape()
    }

  }

  private fun GBrowserToolWindowPanel.selectPopupMenuItem(menuItemText: String) {
    gBrowserPanel {
      buttonByIcon("chevronDown.svg").click()
      wait(1.seconds)
    }

    driver.ui.ideFrame {
      popup().accessibleList().clickItem(menuItemText, false)
      wait(1.seconds)
    }

  }

  private fun Finder.presencesActions() {

    driver.ui.dialog(title = "GBrowser").waitFound(5.seconds).apply {
      checkBox("//div[@text='Highlight URL host']").shouldBe { isSelected() }


      checkBox("//div[@text='Highlight suggestion query']").shouldBe { isSelected() }

      checkBox("//div[@text='Hide URL protocol']").shouldBe { isSelected() }

      checkBox("//div[@text='Load favicon in popup']").shouldBe { isSelected() }

      checkBox("//div[@text='Enable suggestion search']").shouldBe { isSelected().not() }

      okButton.click()
    }
  }
}
