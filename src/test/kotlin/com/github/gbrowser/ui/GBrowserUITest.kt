package com.github.gbrowser.ui

import com.github.gbrowser.ui.fixuters.waitForIndicatorsIgnore
import com.intellij.driver.sdk.ui.components.common.dialogs.newProjectDialog
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.common.welcomeScreen
import com.intellij.driver.sdk.wait
import com.intellij.ide.starter.driver.engine.BackgroundRun
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.config.UseLatestDownloadedIdeBuild
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.event.KeyEvent
import kotlin.time.Duration.Companion.seconds

@Tag("ui")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(UseLatestDownloadedIdeBuild::class)
class GBrowserUITest {

    companion object {

        /**
         * Provide different "service" or "mode" classes if needed.
         * If you only have one environment, just return a single list element.
         */
        @JvmStatic
        fun gBrowserServiceProvider(): List<GBrowserNewUITestBase> {
            return listOf(
                GBrowserServerUI(),      // e.g. new UI
            )
        }
    }


    private lateinit var run: BackgroundRun

    @BeforeEach
    fun initContext() {
        // "UITest" is just a label; you can change it.
        run = Setup.setupTestContext("UITest").runIdeWithDriver()
    }

    @AfterEach
    fun closeIde() {
        run.closeIdeAndWait()
    }

    /**
     * Test for GBrowser tool window functionality
     */
    fun gBrowserToolWindow() {
        val driver = run.driver

        driver.withContext {
            // 1) Welcome Screen Flow
            welcomeScreen {
                createNewProjectButton.click()

                newProjectDialog {
                    chooseProjectType("Java")
                    sampleCodeLabel.click()
                    createButton.click()
                }
            }

            // 2) Inside the IDE
            ideFrame {
                // Wait for indexing, background tasks, etc.
                waitForIndicatorsIgnore(configureOneTimeSettings = true)

                // Wait additional time for background tasks
                wait(5.seconds)

                // Open GBrowser tool window using action menu
                keyboard {
                    // Use KeyEvent constants for key codes
                    hotKey(KeyEvent.VK_ALT, KeyEvent.VK_1) // Open the Project tool window first
                    wait(1.seconds)
                    hotKey(KeyEvent.VK_ALT, KeyEvent.VK_4) // Assuming GBrowser is the 4th tool window, adjust if needed
                }

                wait(6.seconds)

                // Perform some basic actions
                // Note: These actions are simplified and may need adjustment based on actual UI
                keyboard {
                    // Press Tab a few times to navigate through UI elements
                    tab()
                    wait(1.seconds)
                    tab()
                    wait(1.seconds)

                    // Type a URL
                    typeText("https://github.com/")
                    wait(1.seconds)
                    enter()
                }

                wait(5.seconds)
            }
        }
    }

//    /**
//     * Example ParameterizedTest that runs through your "codecovValidation" scenario.
//     */
//    @ParameterizedTest
//    @MethodSource("gBrowserServiceProvider")
//    fun codecovValidation(gBrowserService: GBrowserNewUITestBase) {
//        val driver = run.driver
//
//        driver.withContext {
//            // 1) Welcome Screen Flow
//            welcomeScreen {
//                createNewProjectButton.click() // or however you open the "New Project" wizard
//
//                // If you have multiple ways to create the project, handle them here
//                newProjectDialog {
//
//                    chooseProjectType("Java")
//                    sampleCodeLabel.click()
//
//
//                    createButton.click()
//                }
//            }
//
//            // 2) Inside the IDE
//            ideFrame {
//                // Wait for indexing, background tasks, etc.
//                waitForIndicatorsIgnore(configureOneTimeSettings = true)
//
//                // Create codecov.yml
//                projectView {
//                    projectViewTree.rightClickRow(0) // Usually row 0 is your new project
//                }
//                driver.ui.popupMenuX("//div[@class='HeavyWeightWindow']").select("New")
//                driver.ui.popupMenuX("//div[@class='HeavyWeightWindow']").findMenuItemByTextX("File").click()
//
//                // Insert file name
//                keyboard {
//                    typeText("codecov.yml")
//                    enter()
//                }
//
//                // Write some minimal content into it
//                codeEditor {
//                    // Wait or ensure the editor is open
//                    wait(2.seconds)
//                    keyboard {
//
//                        enter()
//                        typeText("# Just a sample codecov config\nversion: 2.0\n")
//                    }
//                }
//            }
//        }
//    }
}
