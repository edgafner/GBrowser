@file:Suppress("JSUnresolvedReference", "BadExpressionStatementJS")

package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

fun RemoteRobot.idea(function: IdeaFrame.() -> Unit) {
  find(IdeaFrame::class.java, timeout = Duration.ofSeconds(10)).apply(function)
}

@Suppress("unused")
@FixtureName("Idea frame")
@DefaultXpath("IdeFrameImpl type", "//div[@class='IdeFrameImpl']")
class IdeaFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {

  val projectViewTree
    get() = find<ContainerFixture>(byXpath("ProjectViewTree", "//div[@class='ProjectViewTree']"))

  val projectName
    get() = step("Get project name") { return@step callJs<String>("component.getProject().getName()") }

  val menuBar: JMenuBarFixture
    get() = step("Menu...") {
      return@step remoteRobot.find(JMenuBarFixture::class.java, JMenuBarFixture.byType())
    }

  @JvmOverloads
  fun dumbAware(timeout: Duration = Duration.ofMinutes(5), function: () -> Unit) {
    step("Wait for smart mode") {
      waitFor(duration = timeout, interval = Duration.ofSeconds(5)) {
        runCatching { isDumbMode().not() }.getOrDefault(false)
      }
      function()
      step("..wait for smart mode again") {
        waitFor(duration = timeout, interval = Duration.ofSeconds(5)) {
          isDumbMode().not()
        }
      }
    }
  }

  @JvmOverloads
  fun waitForBackgroundTasks(timeout: Duration = Duration.ofMinutes(5)) {
    step("Wait for background tasks to finish") {
      waitFor(duration = timeout, interval = Duration.ofSeconds(5)) {
        findAll<ComponentFixture>(byXpath("//div[@myname='Background process']")).isEmpty() &&
        findAll<ComponentFixture>(byXpath("//div[@class='JProgressBar']")).isEmpty()

      }
    }
  }


  fun isDumbMode(): Boolean {
    return callJs("""
            const frameHelper = com.intellij.openapi.wm.impl.ProjectFrameHelper.getFrameHelper(component)
            if (frameHelper) {
                const project = frameHelper.getProject()
                project ? com.intellij.openapi.project.DumbService.isDumb(project) : true
            } else { 
                true 
            }
        """, true)
  }


  // Show GBrowser Tool window, or leave it open if it is already open
  fun showGBrowserToolWindow() {
    try {
      find<GBrowserToolWindowPanel>(byXpath("//div[@myaction.key='toolwindow.stripe.GBrowser']"), Duration.ofSeconds(8)).click()
    } catch (e: Exception) {
      find<GBrowserToolWindowPanel>(byXpath("//div[@myaction.key='toolwindow.stripe.GBrowser']"), Duration.ofSeconds(8)).click()
    }
  }

  fun showProjectToolWindow() {
    try {
      find<ContainerFixture>(byXpath("ProjectViewTree", "//div[@class='ProjectViewTree']"))
    } catch (e: Exception) {
      find(ComponentFixture::class.java, byXpath("//div[contains(@myaction.key, 'title.project') or contains(@myaction.key,'toolwindow.title.project.view title.project select.in.project')]")).click()
    }
  }
}