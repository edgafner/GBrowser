@file:Suppress("UnstableApiUsage")

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    alias(libs.plugins.kotlin) //`jvm-test-suite`
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)
    kotlin("plugin.serialization") version "2.1.20"
    jacoco
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()


repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"), useInstaller = false
        )
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        jetbrainsRuntime()
        pluginVerifier()

        zipSigner()
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Starter)
        testFramework(TestFrameworkType.JUnit5)
    }

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2") { isTransitive = false }
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2") { isTransitive = false }
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(libs.bundles.kTest)
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    testRuntimeOnly("junit:junit:4.13.2")
    testImplementation("org.kodein.di:kodein-di-jvm:7.25.0")

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.1")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.JETBRAINS
    }
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        freeCompilerArgs.add("-Xjvm-default=all")

    }
}


intellijPlatform {
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
        useJUnitPlatform {
            excludeTags("ui")
        }

        systemProperty("idea.home.path", prepareTestSandbox.get().getDestinationDir().parentFile.absolutePath)
        systemProperty("idea.force.use.core.classloader", "true")

        jvmArgs = listOf(
            "-Didea.trust.all.projects=true",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.desktop/javax.swing=ALL-UNNAMED"
        )

        dependsOn("buildPlugin")
    }

    register<Test>("uiTest") {
        description = "Run only the UI tests that start the IDE"
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        // JUnit 5 filter: only include tests tagged as "ui"
        useJUnitPlatform {
            includeTags("ui")
        }

        systemProperty("path.to.build.plugin", buildPlugin.get().archiveFile.get().asFile.absolutePath)
        systemProperty("idea.home.path", prepareTestSandbox.get().getDestinationDir().parentFile.absolutePath)
        systemProperty(
            "allure.results.directory", project.layout.buildDirectory.get().asFile.absolutePath + "/allure-results"
        )

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
                "-Dshared.indexes.download.auto.consent=true"
            )
        } // If you want this to run after building your plugin:
        dependsOn("buildPlugin")
    }
}


