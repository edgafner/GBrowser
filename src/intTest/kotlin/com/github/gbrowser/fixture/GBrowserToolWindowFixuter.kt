package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import java.time.Duration


fun RemoteRobot.gBrowserToolWindow(timeout: Duration = Duration.ofSeconds(20), function: GBrowserToolWindowPanel.() -> Unit) {
  step("GBrowser Tool Window") {
    find<GBrowserToolWindowPanel>(byXpath("//div[@class='MyNonOpaquePanel']"), timeout).apply(function)
  }
}


@FixtureName("gBrowserToolWindow")
open class GBrowserToolWindowPanel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot,
                                                                                                                        remoteComponent) {

  fun gBrowserPRPanel(timeout: Duration = Duration.ofSeconds(20), function: GBrowserPRPanel.() -> Unit) {
    step("GBrowser PR Panel") {
      find<GBrowserPRPanel>(
        byXpath("//div[@class='GBrowserMainPanel']"), timeout).apply(
        function)
    }
  }



  @Suppress("unused")
  val loginViaToken
    get() = actionLink(byXpath("//div[@class='ActionLink'and @text='Log In with Token…']"))


}

@FixtureName("gBrowserToolWindow")
open class GBrowserPRPanel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot,
                                                                                                                remoteComponent) {

  val quickFilter
    get() = actionButton(byXpath("//div[@myaction.key='review.list.filter.quick.title']"), Duration.ofSeconds(10))
}
