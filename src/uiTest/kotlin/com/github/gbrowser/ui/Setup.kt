package com.github.gbrowser.ui

import com.intellij.ide.starter.buildTool.GradleBuildTool
import com.intellij.ide.starter.community.model.BuildType
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.path.GlobalPaths
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.Starter
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.nio.file.Paths


class Setup {

  companion object {

    init {
      di = DI.Companion {
        extend(di)
        bindSingleton<GlobalPaths>(overrides = true) { GBrowserGlobalPaths() }

      }
    }

    /**
     * Sets up the test context by initializing the necessary objects and configurations.
     */
    fun setupTestContext(hyphenateWithClass: String): IDETestContext {

      val testCase = TestCase(
        IdeProductProvider.IU.copy(buildNumber = "252.27397.103", buildType = BuildType.EAP.type), NoProject
      )
      return Starter.newContext(testName = hyphenateWithClass, testCase = testCase).apply {
        val pluginPath = System.getProperty("path.to.build.plugin")
        PluginConfigurator(this).installPluginFromPath(Paths.get(pluginPath))
        withBuildTool<GradleBuildTool>()
      }.applyVMOptionsPatch {
        addSystemProperty("allure.results.directory", "build/allure-results")
        addSystemProperty("--add-opens", "java.base/java.lang=ALL-UNNAMED")
        addSystemProperty("--add-opens", "java.desktop/javax.swing=ALL-UNNAMED")
        addSystemProperty("awt.useSystemAAFontSettings", "off")
        addSystemProperty("expose.ui.hierarchy.url", true)
        addSystemProperty("hidpi", "false")
        addSystemProperty("ide.browser.jcef.enabled", true)
        addSystemProperty("ide.experimental.ui", true)
        addSystemProperty("ide.mac.file.chooser.native", false)
        addSystemProperty("ide.mac.message.dialogs.as.sheets", false)
        addSystemProperty("ide.native.launcher", true)
        addSystemProperty("ide.show.tips.on.startup.default.value", false)
        addSystemProperty("ide.ui.scale", "1.0")
        addSystemProperty("ide.ui.scale.override", "1.0")
        addSystemProperty("idea.trust.all.projects", true)
        addSystemProperty("jb.consents.confirmation.enabled", false)
        addSystemProperty("jb.privacy.policy.text", "<!--999.999-->")
        addSystemProperty("jbScreenMenuBar.enabled", false)
        addSystemProperty("shared.indexes.download.auto.consent", true)
        addSystemProperty("sun.java2d.uiScale", "1.0")
      }.addProjectToTrustedLocations()
    }
  }
}