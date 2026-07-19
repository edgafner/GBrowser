@file:Suppress("UnstableApiUsage")

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion


fun properties(key: String): Provider<String> = providers.gradleProperty(key)

plugins {
  alias(libs.plugins.kotlin) //`jvm-test-suite`
  alias(libs.plugins.intelliJPlatform)
  alias(libs.plugins.changelog)
  alias(libs.plugins.qodana)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.kover)
  idea
}



group = properties("pluginGroup").get()
version = properties("pluginVersion").get()


repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
  maven("https://cache-redirector.jetbrains.com/packages.jetbrains.team/maven/p/ij/intellij-ide-starter")
  intellijPlatform {
    defaultRepositories()
  }
}


sourceSets.create("uiTest", Action<SourceSet> {
  compileClasspath += sourceSets["main"].output + sourceSets["test"].output
  runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
})


// Configure IntelliJ IDEA to recognize uiTest as test sources
idea {
  module {
    testSources.from(sourceSets["uiTest"].kotlin.srcDirs)
    testResources.from(sourceSets["uiTest"].resources.srcDirs)
  }
}

val uiTestImplementation: Configuration by configurations.getting {
  extendsFrom(configurations.testImplementation.get())
}


val uiTestRuntimeOnly: Configuration by configurations.getting {
  extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies { // IntelliJ Platform dependencies

  intellijPlatform {
    val platformVersion = providers.gradleProperty("platformVersion")
    val platformType = providers.gradleProperty("platformType")
    create(platformType, platformVersion) {
      useInstaller = false
    }
    bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
    jetbrainsRuntime()
    pluginVerifier()
    zipSigner()

    // Test framework dependencies for regular tests
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.JUnit5)

    // Test framework dependencies for UI tests - only Starter needed.
    // Since IJPGP 2.18.0 this also auto-imports the ide-starter-product-* artifacts
    // (e.g. ide-starter-product-idea-ultimate for `IdeInfo.IdeaUltimate`) on platform >= 262.
    testFramework(TestFrameworkType.Starter, configurationName = "uiTestImplementation")

    // 262 + IJPGP 2.16.0 tightened transitive bundled-module resolution; these used to arrive
    // transitively (via the bundled Git4Idea plugin) and must now be declared explicitly.
    bundledModule("intellij.platform.vcs.impl")           // com.intellij.util.ui.InlineIconButton
    bundledModule("intellij.platform.collaborationTools") // com.intellij.collaboration.ui.HorizontalListPanel

    // Since IDE 262 (IJPL-246446) JCEF lives in content modules of the "Web Browser (JCEF)" plugin
    // and is no longer on the default compile classpath. Keep in sync with the <dependencies>
    // block in plugin.xml: ui.jcef owns com.intellij.ui.jcef.*, libraries.jcef owns org.cef.*.
    bundledModule("intellij.platform.ui.jcef")
    bundledModule("intellij.libraries.jcef")
  }

  // Implementation dependencies
  implementation(libs.bundles.jackson) { isTransitive = false }
  compileOnly(libs.kotlinx.serialization.json)


  // Test dependencies
  // ByteBuddy 1.18.4 for Java 25 support (overrides MockK's transitive dependency)
  testImplementation(libs.byte.buddy)
  testImplementation(libs.byte.buddy.agent)
  testRuntimeOnly(libs.junit4)
  testImplementation(libs.bundles.kTest)
  testImplementation(libs.opentest4j)
  testImplementation(libs.bundles.junit5)
  testRuntimeOnly(libs.bundles.junit5Runtime)

  // UI Test dependencies
  // ByteBuddy 1.18.4 for Java 25 support (overrides MockK's transitive dependency)
  uiTestImplementation(libs.byte.buddy)
  uiTestImplementation(libs.byte.buddy.agent)
  uiTestImplementation(libs.coroutines.core)
  uiTestImplementation(libs.kodein.di)
  uiTestImplementation(libs.bundles.kTest)

  // Add JUnit 5 dependencies explicitly
  uiTestImplementation(libs.junit.jupiter)
  uiTestRuntimeOnly(libs.junit.platform.launcher)

  // The auto-added Starter dependencies exclude every library the IDE distribution bundles,
  // but Starter tests run OUTSIDE the IDE process and the squashed starter jar does not
  // shade the TeamCity service-messages library its TeamCityReporter needs on IDE close
  // (NoClassDefFoundError: jetbrains/buildServer/messages/serviceMessages/ServiceMessage).
  // Version pinned to what com.jetbrains.intellij.platform:test-framework-core declares.
  uiTestRuntimeOnly("org.jetbrains.teamcity:serviceMessages:2024.07")
}

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(25)
    vendor = JvmVendorSpec.JETBRAINS
  }

  compilerOptions {
    apiVersion.set(KotlinVersion.KOTLIN_2_3)
    jvmTarget.set(JvmTarget.JVM_25)
    languageVersion.set(KotlinVersion.KOTLIN_2_3)
    freeCompilerArgs.addAll(
      "-Xjvm-default=all",
      "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    )
  }
}


intellijPlatform {
  buildSearchableOptions.set(false)
  pluginConfiguration {
    version = providers.gradleProperty("pluginVersion")
    description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
      val start = "<!-- Plugin description -->"
      val end = "<!-- Plugin description end -->"

      with(it.lines()) {
        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
      }
    }

    val changelog = project.changelog
    changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
      with(changelog) {
        renderItem(
          (getOrNull(pluginVersion) ?: getUnreleased()).withHeader(false).withEmptySections(false),
          Changelog.OutputType.HTML,
        )
      }
    }

    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
      untilBuild = providers.gradleProperty("pluginUntilBuild")

    }
  }

  publishing {
    channels = providers.gradleProperty("pluginVersion").map {
      listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
    }
  }

  pluginVerification {
    failureLevel = listOf(
      org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
      org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES,
    )
  }
}

changelog {
  groups.empty()
  repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
  headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
}


kover {
  reports {
    total {
      xml {
        onCheck = true
      }
      html {
        onCheck = true
      }
    }
  }
}


tasks {
  wrapper {
    gradleVersion = properties("gradleVersion").get()
  }

  runIde {
    jvmArgs = listOf("-Xmx4G")
    systemProperties(
      "ide.native.launcher" to true,
      "ide.browser.jcef.enabled" to true,
      "ide.experimental.ui" to "true",
      "ide.show.tips.on.startup.default.value" to false,
      "idea.trust.all.projects" to true,
      "jb.consents.confirmation.enabled" to false
    )

  }

  publishPlugin {
    dependsOn(patchChangelog)
  }


  test {
    useJUnitPlatform {
      excludeTags("ui")
    }

    // Enable process-level parallelism (safer than method-level parallelism)
    maxParallelForks = minOf(Runtime.getRuntime().availableProcessors() / 2, 3)

    // Keep JUnit execution sequential within each process for stability
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")

    // Increase memory for parallel execution
    minHeapSize = "512m"
    maxHeapSize = "2g"

    systemProperty("idea.home.path", prepareTestSandbox.get().getDestinationDir().parentFile.absolutePath)
    systemProperty("idea.force.use.core.classloader", "true")


    jvmArgs = listOf(
      "-Didea.trust.all.projects=true", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.desktop/javax.swing=ALL-UNNAMED"
    )

    dependsOn("buildPlugin")
  }

  register<Test>("uiTest") {
    description = "Runs only the UI tests that start the IDE"
    group = "verification"

    testClassesDirs = sourceSets["uiTest"].output.classesDirs
    classpath = sourceSets["uiTest"].runtimeClasspath

    useJUnitPlatform {
      includeTags("ui")
    }

    // UI tests should run sequentially (not in parallel) to avoid conflicts
    maxParallelForks = 1

    // Increase memory for UI tests
    minHeapSize = "1g"
    maxHeapSize = "4g"

    systemProperty("path.to.build.plugin", buildPlugin.get().archiveFile.get().asFile.absolutePath)
    systemProperty("idea.home.path", prepareTestSandbox.get().getDestinationDir().parentFile.absolutePath)
    systemProperty("allure.results.directory", project.layout.buildDirectory.get().asFile.absolutePath + "/allure-results")

    // Disable IntelliJ test listener that conflicts with standard JUnit
    systemProperty("idea.test.cyclic.buffer.size", "0")

    jvmArgumentProviders += CommandLineArgumentProvider {
      mutableListOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "-Dexpose.ui.hierarchy.url=true",
        "-Dide.browser.jcef.enabled=true",
        "-Dide.experimental.ui=true",
        "-Dide.mac.file.chooser.native=false",
        "-Dide.mac.message.dialogs.as.sheets=false",
        "-Dide.show.tips.on.startup.default.value=false",
        "-Didea.trust.all.projects=true",
        "-Djb.consents.confirmation.enabled=false",
        "-Djb.privacy.policy.text=<!--999.999-->",
        "-DjbScreenMenuBar.enabled=false",
        "-Djunit.jupiter.extensions.autodetection.enabled=true",
        "-Dshared.indexes.download.auto.consent=true",
      )
    }

    dependsOn(prepareSandbox, buildPlugin)
  }
}
