rootProject.name = "GBrowser"


pluginManagement {
  repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.3"
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree.set("yes")
    publishing.onlyIf { it.buildResult.failures.isNotEmpty() }
  }
}