@file:Suppress("UnstableApiUsage")

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants.Configurations.Attributes
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    alias(libs.plugins.kotlin) //`jvm-test-suite`
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
    kotlin("plugin.serialization") version "2.1.0"
    jacoco
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()


repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        snapshots()
    }
}

sourceSets {
    create("uiTest") {
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

idea {
    module {
        testSources.from(sourceSets["uiTest"].kotlin.srcDirs)
        testResources.from(sourceSets["uiTest"].resources.srcDirs)

    }
}


configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}


val uiTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val uiTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}


configurations.named("uiTestCompileClasspath").configure {
    extendsFrom(configurations.getByName(JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME))
    attributes {
        attribute(Attributes.extracted, true)
        attribute(Attributes.collected, true)
    }
}

configurations.named("uiTestRuntimeClasspath").configure {
    extendsFrom(configurations.getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME))
    extendsFrom(configurations.getByName("intellijPlatformDependency"))
    attributes {
        attribute(Attributes.extracted, true)
        attribute(Attributes.collected, true)
    }
    exclude(group = "junit", module = "junit")
    exclude(group = "com.jetbrains.intellij.platform", module = "test-framework-junit5")
    exclude(group = "com.jetbrains.intellij.platform", module = "test-framework")
    isCanBeResolved = true
}

dependencies {
    intellijPlatform {
    create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"), useInstaller = false)
    bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        instrumentationTools()
        pluginVerifier()
        bundledModule("intellij.platform.vcs.dvcs.impl")

        zipSigner()
        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Platform)
    }

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2") { isTransitive = false }
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2") { isTransitive = false }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(libs.bundles.kTest)
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    testCompileOnly("junit:junit:4.13.2")

    uiTestImplementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    uiTestImplementation("junit:junit:4.13.2")
    uiTestImplementation("org.opentest4j:opentest4j:1.3.0")
    uiTestImplementation(libs.bundles.robot)
    uiTestImplementation(libs.bundles.kTest)
    uiTestImplementation("org.opentest4j:opentest4j:1.3.0")
    uiTestImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    uiTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        freeCompilerArgs.add("-Xjvm-default=all")

    }
}


intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
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

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
}


kover {
    reports {
        total {
            xml { onCheck = true }
            html { onCheck = true }
        }
    }
    currentProject {
        sources {
            excludedSourceSets.addAll(listOf("test", "uiTest"))
        }
    }
}


val runIdeForUiTests by intellijPlatformTesting.runIde.registering {

    task {

        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "-Dide.experimental.ui=true",
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Dide.mac.file.chooser.native=false",
                "-Dide.show.tips.on.startup.default.value=false",
                "-Didea.trust.all.projects=true",
                "-Djb.consents.confirmation.enabled=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-DjbScreenMenuBar.enabled=false",
                "-Djunit.jupiter.extensions.autodetection.enabled=true",
                "-Drobot-server.port=8082",
                "-Dshared.indexes.download.auto.consent=true",
            )
        }

        dependsOn("buildPlugin")
        finalizedBy("runIdeUiCodeCoverageReport")

    }

    plugins { robotServerPlugin() }
}

jacoco {
    toolVersion = "0.8.12"
    applyTo(runIdeForUiTests.get().task.get())
}

runIdeForUiTests.configure {
    task {
        configure<JacocoTaskExtension> {

            // 221+ uses a custom classloader and jacoco fails to find classes
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }


    register<JacocoReport>("runIdeUiCodeCoverageReport") {
        executionData(runIdeForUiTests.get().task.get())
        sourceSets(sourceSets.main.get())
        classDirectories.setFrom(instrumentCode)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIde {
        jvmArgs = listOf("-Xmx4G")
        systemProperties(
            "ide.experimental.ui" to "true",
            "ide.show.tips.on.startup.default.value" to false,
            "idea.trust.all.projects" to true,
            "jb.consents.confirmation.enabled" to false,
        )
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }


    test {
        useJUnitPlatform()
        val skipTestsProvider: Provider<String> = providers.gradleProperty("runUiTests")
        onlyIf("runUiTests property is not set") {
            !skipTestsProvider.isPresent
        }
        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.desktop/sun.awt=ALL-UNNAMED"
        )
    }


    register<Test>("uiTest") {
        description = "Run UI Tests."
        group = "verification"
        testClassesDirs = sourceSets["uiTest"].output.classesDirs
        classpath = sourceSets["uiTest"].runtimeClasspath

        useJUnitPlatform {
            includeTags("uitest")
        }


        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "-Xmx2G" // Increase to 2 GB heap space, adjust as needed
        )


        val skipTestsProvider: Provider<String> = providers.gradleProperty("runUiTests")
        onlyIf("runUiTests property is set") {
            skipTestsProvider.isPresent
        }

        configure<JacocoTaskExtension> {
            isEnabled = false
        }

        outputs.upToDateWhen { false }

    }



    publishPlugin {
        dependsOn("patchChangelog")
        token = environment(
            "PUBLISH_TOKEN"
        ) // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map {
            listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }

    test {
        useJUnitPlatform()
        val skipTestsProvider: Provider<String> = providers.gradleProperty("runUiTests")
        onlyIf("runUiTests property is not set") {
            !skipTestsProvider.isPresent
        }
    }
}


