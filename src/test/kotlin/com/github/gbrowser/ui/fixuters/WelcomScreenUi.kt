package com.github.gbrowser.ui.fixuters

import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitVisible
import com.intellij.driver.sdk.ui.components.common.WelcomeScreenUI
import com.intellij.driver.sdk.ui.components.elements.*
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.ui.ui
import com.intellij.driver.sdk.withRetries
import com.intellij.openapi.util.SystemInfoRt
import kotlin.time.Duration.Companion.seconds


fun WelcomeScreenUI.loginCloneFlow(url: String, token: String) {
  driver.ui.dialog("//div[@title='Clone Repository' and @class='MyDialog']", "Clone Repository") {

    val list = list("//div[@class='VcsCloneDialogExtensionList']")
    list.clickItem("Azure DevOps", false)
    if (SystemInfoRt.isLinux) {
      x { byVisibleText("Configure password store") }.click()
      settingsDialog {
        val textFieldUi = textField("//div[@class='TextFieldWithProcessing']").waitVisible(3.seconds)
        radioButton(locator = "//div[@text='In KeePass']").waitFound(2.seconds).apply {
          if (isSelected.not()) {
            click()
          }
        }
        textFieldUi.apply {
          text = "All-in-one"
        }
        okButton.click()
      }
    }


    textField("//div[@class='ExtendableTextField']").waitFound(3.seconds).apply {
      text = url
    }

    textField("//div[@class='JBPasswordField']").waitFound(3.seconds).apply {
      text = token
    }

    button("Log In").click()
  }
}

fun Finder.withCloneDialog(action: DialogUiComponent.() -> Unit) = driver.ui.dialog("//div[@title='Clone Repository' and @class='MyDialog']").apply(action)

fun DialogUiComponent.cloneRepository(projectName: String, clonePath: String) {
  val listSelector = list("//div[@class='JBList']").waitVisible(5.seconds).shouldBe {
    withRetries("items", 3, f = { items.isNotEmpty() })
  }
  listSelector.clickItem("${projectName}/${projectName}", false)

  textField("//div[@class='ExtendableTextField']").apply {
    text = clonePath
  }
  button("Clone").click()
}
