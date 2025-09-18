package com.github.gbrowser.ui

import com.github.gbrowser.ui.fixtures.GBrowserToolWindowPanel
import com.github.gbrowser.ui.fixtures.gBrowserToolWindow
import com.github.gbrowser.ui.fixtures.showGBrowserToolWindow
import com.github.gbrowser.ui.fixtures.waitForIndicatorsIgnore
import com.github.gbrowser.ui.utils.buttonByIcon
import com.github.gbrowser.ui.utils.cleanEnterTextEnter
import com.github.gbrowser.ui.utils.selectAll
import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.dialogs.newProjectDialog
import com.intellij.driver.sdk.ui.components.common.editor
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.welcomeScreen
import com.intellij.driver.sdk.ui.components.elements.*
import com.intellij.driver.sdk.ui.enabled
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.ui.ui
import com.intellij.driver.sdk.wait
import com.intellij.ide.starter.driver.engine.BackgroundRun
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.config.UseLatestDownloadedIdeBuild
import com.intellij.openapi.diagnostic.logger
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

@Tag("ui")
@ExtendWith(UseLatestDownloadedIdeBuild::class)
class GBrowserUITest {

  companion object {
    val LOG = logger<GBrowserUITest>()
    private val createdProjects = mutableListOf<String>()

    @JvmStatic
    @AfterAll
    fun cleanUpAllProjects() {
      // Clean up any leftover test projects from the IDE's test directory
      val buildDir = Paths.get(System.getProperty("user.dir"), "build", "out", "ide-tests")
      if (Files.exists(buildDir)) {
        try {
          Files.walk(buildDir)
            .filter { path ->
              path.fileName.toString().startsWith("GBrowserTest_") &&
                Files.isDirectory(path)
            }
            .forEach { path ->
              try {
                LOG.info("Cleaning up test project directory: $path")
                path.toFile().deleteRecursively()
              } catch (e: Exception) {
                LOG.warn("Failed to delete test project directory: $path", e)
              }
            }
        } catch (e: Exception) {
          LOG.error("Error during test cleanup", e)
        }
      }

      // Also clean up any projects that might have been created in IdeaProjects
      createdProjects.forEach { projectName ->
        val projectPath = Paths.get(System.getProperty("user.home"), "IdeaProjects", projectName)
        if (Files.exists(projectPath)) {
          try {
            LOG.info("Cleaning up project from IdeaProjects: $projectPath")
            projectPath.toFile().deleteRecursively()
          } catch (e: Exception) {
            LOG.warn("Failed to delete project: $projectPath", e)
          }
        }
      }
      createdProjects.clear()
    }
  }

  private lateinit var run: BackgroundRun
  private lateinit var tempDir: Path
  private lateinit var projectName: String

  @BeforeEach
  fun setupTest() {
    projectName = "GBrowserTest_${System.currentTimeMillis()}"
    createdProjects.add(projectName)

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

      // Also attempt to clean up the project immediately after each test
      cleanUpProjectDirectory(projectName)
    }
  }

  private fun cleanUpProjectDirectory(projectName: String) {
    // Try to clean up from various possible locations
    val possibleLocations = listOf(
      Paths.get(System.getProperty("user.dir"), "build", "out", "ide-tests"),
      Paths.get(System.getProperty("user.home"), "IdeaProjects")
    )

    possibleLocations.forEach { baseDir ->
      if (Files.exists(baseDir)) {
        try {
          Files.walk(baseDir, 2) // Limit depth to avoid excessive traversal
            .filter { path ->
              path.fileName.toString() == projectName ||
                path.fileName.toString().startsWith("$projectName.")
            }
            .filter { Files.isDirectory(it) }
            .forEach { path ->
              try {
                LOG.info("Cleaning up project directory: $path")
                path.toFile().deleteRecursively()
              } catch (e: Exception) {
                LOG.warn("Failed to delete project directory: $path", e)
              }
            }
        } catch (e: Exception) {
          LOG.debug("Error walking directory $baseDir", e)
        }
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

          actionButtonByXpath("//div[contains(@accessiblename, 'Add Tab')]").waitFound(5.seconds).click()

          basicTab()

          browserInitialActions()
        }

        editor {
          rightClick()
          keyboard {
            escape()
          }
        }

        gBrowserToolWindow {
          browserFinalActions()

          gBrowserPanel {

            moveMouse()
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

      actionButtonByXpath("//div[contains(@accessiblename, 'Bookmark')]").waitFound(5.seconds).click()

      actionButtonByXpath("//div[@accessiblename='Reload Page']").waitFound(5.seconds).click()


      actionButtonByXpath("//div[@accessiblename='Backward']").waitFound(5.seconds).click()
    }
  }

  private fun GBrowserToolWindowPanel.browserInitialActions() {

    // Home menu action
    selectPopupMenuItem("Home")

    selectPopupMenuItem("Find...")
    keyboard {
      typeText("abc")
      escape()
    }
  }

  private fun GBrowserToolWindowPanel.browserFinalActions() {

    selectPopupMenuItem("Add to Bo")

    selectPopupMenuItem("Zoom Out")
    selectPopupMenuItem("Zoom In")
    selectPopupMenuItem("Close Tab")


    selectPopupMenuItem("Reload Page")
    selectPopupMenuItem("Preferences")
    presencesActions()
    wait(2.seconds)


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
