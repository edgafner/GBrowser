@file:Suppress("UnstableApiUsage")

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType


fun properties(key: String) = providers.gradleProperty(key)

plugins {
  alias(libs.plugins.kotlin) //`jvm-test-suite`
  alias(libs.plugins.intelliJPlatform)
  alias(libs.plugins.changelog)
  alias(libs.plugins.qodana)
  kotlin("plugin.serialization") version "2.2.0"
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
    testFramework(TestFrameworkType.Starter)
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.JUnit5)

    // Test framework dependencies for UI tests - only Starter needed
    testFramework(TestFrameworkType.Starter, configurationName = "uiTestImplementation")
  }

  // Implementation dependencies
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2") { isTransitive = false }
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.2") { isTransitive = false }
  implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2") { isTransitive = false }
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.2") { isTransitive = false }
  compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")


  testRuntimeOnly("junit:junit:4.13.2")
  testImplementation(libs.bundles.kTest)
  testImplementation("org.opentest4j:opentest4j:1.3.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

  // Add JUnit 5 dependencies for tests
  testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")

  // UI Test dependencies
  uiTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
  uiTestImplementation("org.kodein.di:kodein-di-jvm:7.26.1")
  uiTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
  uiTestImplementation(libs.bundles.kTest)

  // Add JUnit 5 dependencies explicitly
  uiTestImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
  uiTestRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.JETBRAINS
  }

  compilerOptions {
    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
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
    }
  }

  pluginVerification {
    ides {
      recommended()
    }
  }

  signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
  }

  publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN")
    channels = providers.gradleProperty("pluginVersion").map {
      listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
    }
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
      listOf(
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
