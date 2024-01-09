@file:Suppress("JSUnresolvedReference", "BadExpressionStatementJS")

package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import java.awt.Point
import java.time.Duration

fun RemoteRobot.idea(function: IdeaFrame.() -> Unit) {
  find(IdeaFrame::class.java, timeout = Duration.ofSeconds(10)).apply(function)
}

@FixtureName("Idea frame")
@DefaultXpath("IdeFrameImpl type", "//div[@class='IdeFrameImpl']")
class IdeaFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {

  val projectViewTree
    get() = find<ContainerFixture>(byXpath("ProjectViewTree", "//div[@class='ProjectViewTree']"))

  val projectName
    get() = step("Get project name") { return@step callJs<String>("component.getProject().getName()") }





  fun waitForBackgroundTasks(timeout: Duration = Duration.ofMinutes(5)) {
    step("Wait for background tasks to finish") {
      waitFor(duration = timeout, interval = Duration.ofSeconds(5)) { // search for the progress bar
        find<ComponentFixture>(byXpath("//div[@class='InlineProgressPanel']")).findAllText().isEmpty()
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
    }
    catch (e: Exception) {
      find<GBrowserToolWindowPanel>(byXpath("//div[@myaction.key='toolwindow.stripe.GBrowser']"), Duration.ofSeconds(8)).click()
    }
  }

  // Show GBrowser Tool window, or leave it open if it is already open
  fun showGBrowserDevToolsToolWindow() {
    try {
      find<GBrowserToolWindowPanel>(byXpath("//div[@myaction.key='toolwindow.stripe.GDevTools']"), Duration.ofSeconds(8)).click()
    }
    catch (e: Exception) {
      find<GBrowserToolWindowPanel>(byXpath("//div[@myaction.key='toolwindow.stripe.GDevTools']"), Duration.ofSeconds(8)).click()
    }
  }


  fun showProjectToolWindow() {
    try {
      find<ContainerFixture>(byXpath("ProjectViewTree", "//div[@class='ProjectViewTree']"))
    }
    catch (e: Exception) {
      find(ComponentFixture::class.java, byXpath(
        "//div[contains(@myaction.key, 'title.project') or contains(@myaction.key,'toolwindow.title.project.view title.project select.in.project')]")).click()
    }
  }

  fun dragAndDrop(endPoint: Point) = step("Drag and Drop from to $endPoint") {
    remoteRobot.runJs("""
                    const pointEnd = new Point(${endPoint.x}, ${endPoint.y})
                    try {
                        robot.pressMouse(MouseButton.LEFT_BUTTON)
                        Thread.sleep(500)
                        robot.moveMouse(pointEnd)
                    } finally {
                        Thread.sleep(500)
                        robot.releaseMouse(MouseButton.LEFT_BUTTON)  
                    }
                """)
  }
}