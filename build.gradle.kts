import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {

  id("java") // Java support
  alias(libs.plugins.kotlin) // Kotlin support
  alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
  alias(libs.plugins.changelog) // Gradle Changelog Plugin
  alias(libs.plugins.qodana) // Gradle Qodana Plugin
  alias(libs.plugins.kover) // Gradle Kover Plugin
  kotlin("plugin.serialization") version "1.9.22"
  jacoco
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
  mavenCentral()
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

sourceSets {
  create("uiTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
  }
}

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

dependencies {
  implementation(libs.annotations)

  testImplementation(libs.bundles.kTest)

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1") {
    isTransitive = false
  }

  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

  uiTestImplementation("com.intellij.remoterobot:remote-fixtures:0.11.21")

  uiTestImplementation("com.intellij.remoterobot:remote-robot:0.11.21")

  uiTestImplementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  uiTestImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
  uiTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

kotlin {
  jvmToolchain(17)

}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {

  pluginName = properties("pluginName")
  version = properties("platformVersion")
  type = properties("platformType")

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  groups.empty()
  repositoryUrl = properties("pluginRepositoryUrl")
  headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
  cachePath = provider { file(".qodana").canonicalPath }
  reportPath = provider { file("build/reports/inspections").canonicalPath }
  saveReport = true
  showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
}


// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
koverReport {
  defaults {
    xml {
      onCheck = true
    }
    html {
      onCheck = true
    }
  }
}

kover {
  excludeSourceSets {
    names("test", "uiTest")
  }
}

jacoco {
  toolVersion = "0.8.10"
  applyTo(tasks.runIdeForUiTests.get())

}

tasks {

  wrapper {
    gradleVersion = properties("gradleVersion").get()
  }

  patchPluginXml {
    version = properties("pluginVersion")
    sinceBuild = properties("pluginSinceBuild")
    untilBuild = properties("pluginUntilBuild")

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
      val start = "<!-- Plugin description -->"
      val end = "<!-- Plugin description end -->"

      with(it.lines()) {
        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
      }
    }

    val changelog =
      project.changelog // local variable for configuration cache compatibility // Get the latest available change notes from the changelog file
    changeNotes = properties("pluginVersion").map { pluginVersion ->
      with(changelog) {
        renderItem(
          (getOrNull(pluginVersion) ?: getUnreleased()).withHeader(false).withEmptySections(false),
          Changelog.OutputType.HTML,
        )
      }
    }
  }

  runIde {
    jvmArgs = listOf("-Xmx4G")
    systemProperty("ide.experimental.ui", "true")
    systemProperty("ide.show.tips.on.startup.default.value", false)
    systemProperty("idea.trust.all.projects", true)
    systemProperty("jb.consents.confirmation.enabled", false)
    systemProperty("ide.browser.jcef.enabled", true)
    systemProperty("ide.browser.jcef.headless.enabled", true)
    systemProperty("ide.browser.jcef.testMode.enabled", true)
  }

  val runIdeUiCodeCoverageReport = register<JacocoReport>("runIdeUiCodeCoverageReport") {
    executionData(runIdeForUiTests.get())
    sourceSets(sourceSets.main.get())

    reports {
      xml.required = true
      html.required = true
    }
  }

  // Configure UI tests plugin
  // Read more: https://github.com/JetBrains/intellij-ui-test-robot
  runIdeForUiTests {
    systemProperty("ide.browser.jcef.enabled", true)
    systemProperty("ide.browser.jcef.headless.enabled", true)
    systemProperty("ide.browser.jcef.testMode.enabled", true)
    systemProperty("ide.experimental.ui", true)
    systemProperty("apple.laf.useScreenMenuBar", false)
    systemProperty("ide.mac.file.chooser.native", false)
    systemProperty("ide.mac.message.dialogs.as.sheets", false)
    systemProperty("ide.show.tips.on.startup.default.value", false)
    systemProperty("idea.trust.all.projects", true)
    systemProperty("jb.consents.confirmation.enabled", false)
    systemProperty("jb.privacy.policy.text", "<!--999.999-->")
    systemProperty("jbScreenMenuBar.enabled", false) //systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
    systemProperty("robot-server.port", 8082) //systemProperty("shared.indexes.download.auto.consent", true)

    jvmArgs("--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED")


    configure<JacocoTaskExtension> {

      // sync with testing-sub plugin
      // 221+ uses a custom classloader and jacoco fails to find classes
      isIncludeNoLocationClasses = true
      excludes = listOf("jdk.internal.*")
    }

    finalizedBy(runIdeUiCodeCoverageReport)
  }

  downloadRobotServerPlugin {
    version = "0.11.20"
  }

  signPlugin {
    certificateChain = environment("CERTIFICATE_CHAIN")
    privateKey = environment("PRIVATE_KEY")
    password = environment("PRIVATE_KEY_PASSWORD")
  }

  publishPlugin {
    dependsOn("patchChangelog")
    token = environment(
      "PUBLISH_TOKEN") // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
    channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
  }

  test {
    useJUnitPlatform()
    val skipTestsProvider: Provider<String> = providers.gradleProperty("runUiTests")
    onlyIf("runUiTests property is not set") {
      !skipTestsProvider.isPresent
    }
  }

  task<Test>("uiTest") {
    description = "Runs ui tests."
    group = "verification"

    testClassesDirs = sourceSets["uiTest"].output.classesDirs
    classpath = sourceSets["uiTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform {
      includeTags("uitest")
    }

    val skipTestsProvider: Provider<String> = providers.gradleProperty("runUiTests")
    onlyIf("runUiTests property is set") {
      skipTestsProvider.isPresent
    }

    configure<JacocoTaskExtension> {
      // sync with testing-subplugin
      isEnabled = false
    }

  }

}

