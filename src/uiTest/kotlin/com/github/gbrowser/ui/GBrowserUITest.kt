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
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
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
        deleteTestDirectoriesSafely(buildDir, "GBrowserTest_")
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

    /**
     * Safely deletes test directories using FileVisitor to handle race conditions
     * where files may be deleted by other processes during traversal.
     */
    private fun deleteTestDirectoriesSafely(baseDir: Path, prefix: String) {
      // First, collect directories to delete (don't delete while walking)
      val directoriesToDelete = mutableListOf<Path>()

      try {
        Files.walkFileTree(baseDir, object : SimpleFileVisitor<Path>() {
          override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (dir != baseDir && dir.fileName.toString().startsWith(prefix)) {
              directoriesToDelete.add(dir)
              return FileVisitResult.SKIP_SUBTREE // Don't descend into directories we'll delete
            }
            return FileVisitResult.CONTINUE
          }

          override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
            // Ignore files that disappeared during traversal (race condition with IDE shutdown)
            LOG.debug("File disappeared during cleanup traversal: $file")
            return FileVisitResult.CONTINUE
          }
        })
      } catch (e: Exception) {
        LOG.warn("Error walking directory for cleanup: $baseDir", e)
      }

      // Now delete the collected directories
      directoriesToDelete.forEach { path ->
        try {
          LOG.info("Cleaning up test project directory: $path")
          deleteDirectoryRecursively(path)
        } catch (e: Exception) {
          LOG.warn("Failed to delete test project directory: $path", e)
        }
      }
    }

    /**
     * Recursively deletes a directory, handling race conditions gracefully.
     */
    private fun deleteDirectoryRecursively(directory: Path) {
      if (!Files.exists(directory)) return

      try {
        Files.walkFileTree(directory, object : SimpleFileVisitor<Path>() {
          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            try {
              Files.deleteIfExists(file)
            } catch (e: NoSuchFileException) {
              // File was already deleted, ignore
            } catch (e: IOException) {
              LOG.debug("Could not delete file: $file", e)
            }
            return FileVisitResult.CONTINUE
          }

          override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
            // File disappeared during traversal, continue
            return FileVisitResult.CONTINUE
          }

          override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            try {
              Files.deleteIfExists(dir)
            } catch (e: DirectoryNotEmptyException) {
              // Directory not empty, some files couldn't be deleted
              LOG.debug("Directory not empty, skipping: $dir")
            } catch (e: NoSuchFileException) {
              // Directory was already deleted, ignore
            } catch (e: IOException) {
              LOG.debug("Could not delete directory: $dir", e)
            }
            return FileVisitResult.CONTINUE
          }
        })
      } catch (e: Exception) {
        // Fall back to simple delete if walkFileTree fails
        try {
          directory.toFile().deleteRecursively()
        } catch (e2: Exception) {
          LOG.debug("Fallback delete also failed for: $directory", e2)
        }
      }
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
        // Collect directories first, then delete (avoid race conditions)
        val directoriesToDelete = mutableListOf<Path>()

        try {
          Files.walkFileTree(baseDir, setOf(), 2, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
              val fileName = dir.fileName?.toString() ?: return FileVisitResult.CONTINUE
              if (fileName == projectName || fileName.startsWith("$projectName.")) {
                directoriesToDelete.add(dir)
                return FileVisitResult.SKIP_SUBTREE
              }
              return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
              return FileVisitResult.CONTINUE // Ignore files that disappeared
            }
          })
        } catch (e: Exception) {
          LOG.debug("Error walking directory $baseDir", e)
        }

        // Delete collected directories
        directoriesToDelete.forEach { path ->
          try {
            LOG.info("Cleaning up project directory: $path")
            deleteDirectoryRecursively(path)
          } catch (e: Exception) {
            LOG.warn("Failed to delete project directory: $path", e)
          }
        }
      }
    }
  }

  private fun deleteDirectoryRecursively(directory: Path) {
    // Delegate to companion object's implementation
    Companion.deleteDirectoryRecursively(directory)
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
      typeText("ab")
      escape()
      escape()
    }
  }

  private fun GBrowserToolWindowPanel.browserFinalActions() {

    //selectPopupMenuItem("Add to Bo")

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
