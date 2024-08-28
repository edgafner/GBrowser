package com.github.gbrowser.ui

import com.github.gbrowser.UITest
import com.github.gbrowser.extensions.RemoteRobotExtension
import com.github.gbrowser.extensions.StepsLogger
import com.github.gbrowser.fixture.*
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import com.intellij.remoterobot.utils.waitForIgnoringError
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Point
import java.nio.file.Path
import java.time.Duration.ofMinutes

@UITest
@ExtendWith(RemoteRobotExtension::class)
class GBrowserUITest {

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
        this.runJs(
            """
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
        """, true
        )
        try {
            idea {

                dialogByXpath("//div[@title.key='exit.confirm.title' and @class='MyDialog']") {
                    button("Exit").click()
                }
            }
        } catch (ignored: Exception) { // No confirm dialog

        }
        Thread.sleep(2_000)
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
            initWaiting()

            showGBrowserToolWindow()

            Thread.sleep(6_000)

            button(byXpath("//div[@myicon='add.svg']")).click()

            Thread.sleep(3_000)
            basicTab(this)

            browserActions(this)

            presencesActions(this)


        }
    }


    private fun IdeaFrame.initWaiting() {
        waitFor(ofMinutes(5)) { isDumbMode().not() }
        Thread.sleep(5_000)
        waitForBackgroundTasks()
        Thread.sleep(4_000)
    }

    private fun RemoteRobot.basicTab(ideaFrame: IdeaFrame) {
        step("Basic actions") {
            gBrowserToolWindow {

                gBrowserToolWindowMyNonOpaquePanel {
                    val dimension = toolWindowDimension
                    val location = location
                    moveMouse(location)
                    moveMouse(Point(dimension.width, location.y))
                    ideaFrame.dragAndDrop(Point(location.x + dimension.width + dimension.width, location.y))

                    gBrowserPRPanel {
                        textField(byXpath("//div[@class='TextFieldWithProcessing']")).text = "https://github.com/"
                        textField(byXpath("//div[@class='TextFieldWithProcessing']")).keyboard {
                            enter()
                        }
                        textField(byXpath("//div[@class='TextFieldWithProcessing']")).text = "https://github.com/"
                        textField(byXpath("//div[@class='TextFieldWithProcessing']")).keyboard {
                            enter()
                        }
                    }

                    Thread.sleep(5_000)

                    gBrowserPRPanel { //
                        button(byXpath("//div[@myicon='bookmark_remove.svg']")).click()
                        moveMouse(location)
                        click()
                        keyboard {
                            escape()
                        }
                        button(byXpath("//div[@myicon='refresh.svg']")).click()
                        assert(button(byXpath("//div[@myicon='left.svg']")).isEnabled())
                    }
                }
            }
        }
    }

    private fun RemoteRobot.browserActions(ideaFrame: IdeaFrame) {
        step("Validate various actions") {
            gBrowserToolWindow {
                gBrowserToolWindowMyNonOpaquePanel {
                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Home", false)
                    }
                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Find...", false)
                        keyboard {
                            escape()
                        }

                    }
                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.collectItems().forEach {
                            LOG.info("Item: --${it}--")
                        }
                        itemList.clickItem("Reload Pa", false)
                    }

                    Thread.sleep(2_000)
                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.collectItems().forEach {
                            LOG.info("Item: --${it}--")
                        }
                        itemList.clickItem("Add to Bo", false)
                    }

                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Open Dev", false)
                    }

                    ideaFrame.showGBrowserDevToolsToolWindow()

                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Zoom Out", false)
                    }

                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Zoom In", false)
                    }

                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Close Tab", false)
                    }

                    gBrowserPRPanel {
                        assert(button(byXpath("//div[@tooltiptext='https://github.com/']")).isShowing)
                    }

                    gBrowserPRPanel {
                        button(byXpath("//div[@myicon='chevronDown.svg']")).click()
                        val itemList = ideaFrame.heavyWeightWindow().itemsList
                        itemList.clickItem("Preferences", false)
                    }
                }
            }
        }
    }

    private fun RemoteRobot.presencesActions(ideaFrame: IdeaFrame) {
        step("Validate preferences actions") {

            with(ideaFrame.dialog("GBrowser")) {
                assert(checkBox(byXpath("//div[@text='Highlight URL host']")).isSelected())
                assert(checkBox(byXpath("//div[@text='Highlight suggestion query']")).isSelected())
                assert(checkBox(byXpath("//div[@text='Hide URL protocol']")).isSelected())
                assert(checkBox(byXpath("//div[@text='Load favicon in popup']")).isSelected())
                assert(checkBox(byXpath("//div[@text='Enable suggestion search']")).isSelected().not())
                assert(checkBox(byXpath("//div[@text='Life Span in new tab']")).isSelected())

                assert(checkBox(byXpath("//div[@text='History enable']")).isSelected())
                assert(checkBox(byXpath("//div[@text='Debug port']")).isSelected().not())
                assert(checkBox(byXpath("//div[@text='Hide toolwindow label']")).isSelected())
                assert(checkBox(byXpath("//div[contains(@text, 'toolbar')]")).isSelected())
                checkBox(byXpath("//div[contains(@text, 'toolbar')]")).click()

                button("OK").click()
            }

            gBrowserToolWindow {

                gBrowserToolWindowMyNonOpaquePanel {
                    gBrowserPRPanel {
                        assertThrows<WaitForConditionTimeoutException> {
                            button(byXpath("//div[@tooltiptext='https://github.com/']")).isShowing

                        }
                    }
                }
            }
        }
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger("GBrowserTollWindowUITest")
    }

}

