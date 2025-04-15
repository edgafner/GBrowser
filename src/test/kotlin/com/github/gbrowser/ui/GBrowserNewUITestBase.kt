package com.github.gbrowser.ui

import java.time.LocalDate
import java.util.*

abstract class GBrowserNewUITestBase {
  protected val generatedBranchName: String by lazy {
    val envBranchName = System.getenv("UI_BRANCH_NAME")
    if (envBranchName != null) {
      envBranchName.lowercase().replace(" ", "-") + "-${LocalDate.now()}-${Random().nextInt(1000)}"
    } else {
      "${System.getProperty("os.name").lowercase().replace(" ", "-")}-${LocalDate.now()}-${Random().nextInt(1000)}"
    }
  }


  // Make this accessible to AZDUITestActions
  abstract fun getBranchName(): String
  abstract fun getTestName(): String

  // These methods were already abstract
  abstract fun getCurrentFolderName(): String
  abstract fun getFolderName(): String
  abstract fun getProjectName(): String
  abstract fun getAzureDevOpsUrl(): String


  companion object {
    const val UI_TEXT_FILE = "ui.txt"
    val baseFolder: String by lazy {
      "azd-base-${kotlin.random.Random.nextInt(1000)}"
    }
  }
}