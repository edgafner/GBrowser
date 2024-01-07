package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import java.awt.Dimension
import java.awt.Point
import java.time.Duration


fun RemoteRobot.gBrowserToolWindow(timeout: Duration = Duration.ofSeconds(20), function: GBrowserToolWindowPanel.() -> Unit) {
    step("GBrowser Tool Window") {
    find<GBrowserToolWindowPanel>(byXpath("//div[@accessiblename='GBrowser']"), timeout).apply(function)
  }
}

fun RemoteRobot.gBrowserToolWindowMyNonOpaquePanel(timeout: Duration = Duration.ofSeconds(20),
                                                   function: GBrowserToolWindowPanel.() -> Unit) {
  step("GBrowser Tool Window") {
    find<GBrowserToolWindowPanel>(byXpath("//div[@class='MyNonOpaquePanel'][.//div[@class='MyPanel']]"), timeout).apply(function)
  }
}


@FixtureName("gBrowserToolWindow")
open class GBrowserToolWindowPanel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot,
                                                                                                                        remoteComponent) {

  fun gBrowserPRPanel(timeout: Duration = Duration.ofSeconds(20), function: GBrowserPRPanel.() -> Unit) {
    step("GBrowser Panel") {
      find<GBrowserPRPanel>(byXpath("//div[@class='GBrowserToolWindowBrowser']"), timeout).apply(function)
    }
  }


  val toolWindowDimension: Dimension
    get() {
      return callJs("component.getSize();", true)
    }

  val location: Point
    get() {
      return callJs("component.getLocation();", true)
    }

}

@FixtureName("gBrowserToolWindow")
open class GBrowserPRPanel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot,
                                                                                                                remoteComponent)
