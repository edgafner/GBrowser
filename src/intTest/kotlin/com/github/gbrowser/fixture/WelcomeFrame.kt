package com.github.gbrowser.fixture

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import java.nio.file.Path
import java.time.Duration


fun RemoteRobot.welcomeFrame(function: WelcomeFrame.() -> Unit) {
  find(WelcomeFrame::class.java, Duration.ofSeconds(10)).apply(function)
}

@Suppress("unused")
@FixtureName("Welcome Frame")
@DefaultXpath("type", "//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
  val createNewProjectLink
    get() = actionLink(byXpath("New Project",
                               "//div[(@class='MainButton' and @text='New Project') or (@accessiblename='New Project' and @class='JButton')]"))

  val openProjectLink
    get() = actionLink(byXpath("Open", "//div[@accessiblename.key='action.WelcomeScreen.OpenProject.text']"))

  val fromCSV
    get() = actionLink(byXpath("Get from VCS", "//div[@accessiblename.key='action.Vcs.VcsClone.text']"))

  val moreActions
    get() = button(byXpath("More Action", "//div[@accessiblename='More Actions']"))

  val heavyWeightPopup
    get() = remoteRobot.find(ComponentFixture::class.java, byXpath("//div[@class='HeavyWeightWindow']"))

  fun openFolder(path: Path) {
    fileBrowser("Open") {
      selectFile(path)
    }
  }
}
