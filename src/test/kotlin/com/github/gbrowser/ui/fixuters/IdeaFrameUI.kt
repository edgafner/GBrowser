package com.github.gbrowser.ui.fixuters

import com.github.gbrowser.ui.utils.cleanEnterTextEnter
import com.intellij.driver.sdk.ui.QueryBuilder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI
import com.intellij.driver.sdk.ui.components.elements.checkBox
import com.intellij.driver.sdk.ui.components.elements.tree
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.ui.xQuery
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.openapi.diagnostic.logger
import kotlin.time.Duration.Companion.seconds

private val LOG = logger<IdeaFrameUI>()

fun IdeaFrameUI.waitForIndicatorsIgnore(configureOneTimeSettings: Boolean) {
    try {
        driver.waitForIndicators(180.seconds)
    } catch (_: Exception) {
        LOG.info("Indicators not found")
    }
    if (configureOneTimeSettings) {
        configureSettings()
    }
}

fun IdeaFrameUI.configureSettings() {
    openSettingsDialog()
    settingsDialog { // Type "commit" to filter the setting
        /*textField("//div[@class='TextFieldWithProcessing']").waitFound(3.seconds).apply {
          text = "commit"
        }

        checkBox("//div[@text='Use non-modal commit interface']").waitFound(3.seconds).uncheck()*/

        val textField = xx("//div[@class='TextFieldWithProcessing']").list().first()
        with(textField) {
            waitFound(3.seconds).doubleClick()
            cleanEnterTextEnter("All-in-one")
        }

        checkBox("//div[@accessiblename='All-in-one Diff for Code Reviews']").waitFound(3.seconds).uncheck()

        with(textField) {
            waitFound(3.seconds).doubleClick()
            cleanEnterTextEnter("AI Settings")
        }

        checkBox("//div[@accessiblename='Enable AI assistance']").waitFound(3.seconds).check()

        okButton.click()
    }
}


fun IdeaFrameUI.projectView(
    locator: QueryBuilder.() -> String = {
        componentWithChild(
            byType("com.intellij.toolWindow.InternalDecoratorImpl"),
            byType("com.intellij.ide.projectView.impl.ProjectViewTree")
        )
    },
    action: ProjectViewToolWindowUi.() -> Unit = {},
): ProjectViewToolWindowUi = x(ProjectViewToolWindowUi::class.java, locator).apply(action)

class ProjectViewToolWindowUi(data: ComponentData) : UiComponent(data) {
    val projectViewTree = tree(xQuery { byType("com.intellij.ide.projectView.impl.ProjectViewTree") })
}