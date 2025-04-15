package com.github.gbrowser.ui

import kotlin.random.Random

class GBrowserServerUI : GBrowserNewUITestBase() {
    override fun getBranchName(): String {
        return generatedBranchName
    }

    val generateFolderName: String = "${Random.nextInt(1000)}"

    override fun getCurrentFolderName(): String {
        return "${getProjectName()}-${generateFolderName}"
    }

    override fun getFolderName(): String {
        return "${baseFolder}/${getCurrentFolderName()}"
    }

    override fun getTestName(): String {
        return "QFServerUI"
    }

    override fun getProjectName() = "azd-ui-test"
    override fun getAzureDevOpsUrl() = "https://azd-plugin/DefaultCollection"
}