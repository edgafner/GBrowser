package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import java.time.Duration

fun ContainerFixture.dialog(title: String,
                            timeout: Duration = Duration.ofSeconds(20),
                            function: DialogFixture.() -> Unit = {}): DialogFixture = step("Search for dialog with title $title") {
  find<DialogFixture>(DialogFixture.byTitle(title), timeout).apply(function)
}

fun ContainerFixture.dialogByXpath(xpath: String,
                                   timeout: Duration = Duration.ofSeconds(20),
                                   function: DialogFixture.() -> Unit = {}): DialogFixture = step("Search for dialog with xpath") {
  find<DialogFixture>(DialogFixture.byXPath(xpath), timeout).apply(function)
}

@FixtureName("Dialog")
open class DialogFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot,
                                                                                                              remoteComponent) {

  companion object {

    @JvmStatic
    fun byXPath(xpath: String) = byXpath(xpath)

    @JvmStatic
    fun byTitle(title: String) = byXpath("title $title", "//div[@title='$title' and @class='MyDialog']")


    @JvmStatic
    fun byTitleContains(partial: String) = byXpath("partial title $partial",
                                                   "//div[contains(@accessiblename, $partial) and @class='MyDialog']")

  }

  fun setToken(password: String) {
    find<JTextFieldFixture>(byXpath("//div[@class='JBPasswordField']"), Duration.ofSeconds(20)).text = password
  }


  fun close() = runJs("robot.close(component)")

  open fun pressOk() = pressButton("OK")

  fun pressCancel() = pressButton("Cancel")

  fun pressButton(text: String) = button(text).click()


  val title: String
    get() = callJs("component.getTitle();")
}