package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitForIgnoringError
import java.nio.file.Path
import java.time.Duration

fun ContainerFixture.fileBrowser(partialTitle: String,
                                 timeout: Duration = Duration.ofSeconds(20),
                                 function: FileBrowserFixture.() -> Unit = {}) {
  step("Search for file explorer with title matching $partialTitle") {
    val dialog = find<FileBrowserFixture>(DialogFixture.byTitleContains(partialTitle), timeout)

    dialog.apply(function)

    if (dialog.isShowing) {
      dialog.close()
    }

    dialog
  }
}

@FixtureName("FileBrowser")
class FileBrowserFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : DialogFixture(remoteRobot, remoteComponent) {

  private val tree by lazy {
    find<JTreeFixture>(byXpath("//div[@class='Tree']"), Duration.ofSeconds(10))
  }

  fun selectFile(path: Path) {
    step("Select $path") {
      step("Refresh file explorer to make sure the file ${path.fileName} is loaded") {
        waitForIgnoringError(duration = Duration.ofSeconds(30), interval = Duration.ofSeconds(10)) {
          findAndClick("//div[@accessiblename='Refresh']")
          setFilePath(path)
          tree.clickRowWithText(path.fileName.toString(), false)
          true
        }
        pressOk()
      }
    }
  }

  private fun setFilePath(path: Path) {
    step("Set file path to $path") {
      val pathBox: JTextFieldFixture = find(byXpath("//div[@class='BorderlessTextField']"), Duration.ofSeconds(5))
      pathBox.text = ""
      keyboard { this.enterText(path.toString()) }
    }
  }

}
