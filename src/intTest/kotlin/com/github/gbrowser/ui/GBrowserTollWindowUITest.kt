package com.github.gbrowser.ui


import com.github.gbrowser.UITest
import com.github.gbrowser.extensions.RemoteRobotExtension
import com.github.gbrowser.extensions.StepsLogger
import com.github.gbrowser.fixture.*
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.intellij.remoterobot.utils.waitForIgnoringError
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Duration.ofMinutes

@ExtendWith(RemoteRobotExtension::class)
@UITest
class GBrowserTollWindowUITest {

  @TempDir
  lateinit var tempDir: Path

  init {
    StepsLogger.init()
  }

  @BeforeEach
  fun waitForIde(remoteRobot: RemoteRobot) {
    waitForIgnoringError(ofMinutes(3)) {
      remoteRobot.callJs("true")
    }
  }

  @Suppress("JSUnresolvedReference")
  @AfterEach
  fun closeProject(remoteRobot: RemoteRobot) = with(remoteRobot) {
    this.runJs("""
            importClass(com.intellij.openapi.application.ApplicationManager)

            const actionId = "Exit";
            const actionManager = com.intellij.openapi.actionSystem.ActionManager.getInstance();
            const action = actionManager.getAction(actionId);
            
            const runAction = new Runnable({
                run: function() {
                    actionManager.tryToExecute(action, com.intellij.openapi.ui.playback.commands.ActionCommand.getInputEvent(actionId), null, null, true);
                }
            })
            ApplicationManager.getApplication().invokeLater(runAction)
        """, true)
    try {
      idea {

        dialog("Confirm Exit") {
          button("Exit").click()
        }
      }
    }
    catch (ignored: Exception) { // No confirm dialog
    }
  }


  @Test
  fun gBrowserToolWindow(remoteRobot: RemoteRobot) = with(remoteRobot) {

    welcomeFrame {
      createNewProjectLink.click()
      dialog("New Project") {
        findText("Java").click()
        checkBox("Add sample code").select()
        textField(byXpath("//div[@class='ExtendableTextField']")).text = tempDir.toAbsolutePath().toString()
        button("Create").click()
      }
    }

    idea {
      waitFor(ofMinutes(5)) { isDumbMode().not() }

      Thread.sleep(10_000)

      showGBrowserToolWindow()


      waitFor(ofMinutes(5)) { isDumbMode().not() } //waitForBackgroundTasks()
      Thread.sleep(5_000)
      val th = find<ComponentFixture>(byXpath("//div[@class='ContentTabLabel' and @text='GBrowser']"))
      click(th.locationOnScreen)
      Thread.sleep(1_000)

      val toolWindowHeader = find<ComponentFixture>(
        byXpath("//div[@class='ToolWindowHeader']//div[@classhierarchy='javax.swing.JPanel -> javax.swing.JComponent']"))
      moveMouse(toolWindowHeader.locationOnScreen)
      button(byXpath("//div[@myicon='add.svg']")).click()
      Thread.sleep(3_000)


      gBrowserToolWindow {
        gBrowserPRPanel {
          if (remoteRobot.isMac()) {
            textField(byXpath("//div[@class='TextFieldWithProcessing']")).text = "https://www.google.com"
          }
          else {
            textField(byXpath("//div[@class='TextFieldWithProcessing']")).text = ""
            textField(byXpath("//div[@class='TextFieldWithProcessing']")).click()
            keyboard {
              enterText("https://www.google.com")
              enter()
            }
          }




          Thread.sleep(5_000)
          rightClick()
          keyboard {
            enterText("A")
            enter()
          }
        }
      }
      with(textEditor()) {

        click()
      }

      gBrowserToolWindow {
        gBrowserPRPanel {
          Thread.sleep(10_000)
          button(byXpath("//div[@myicon='left.svg']")).isEnabled()
          button(byXpath("//div[@accessiblename='https://www.google.com/']")).isEnabled()

        }
      }

    }
  }
}

