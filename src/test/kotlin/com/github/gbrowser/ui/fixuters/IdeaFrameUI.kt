package com.github.gbrowser.ui.fixuters

import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.openapi.diagnostic.logger
import kotlin.time.Duration.Companion.seconds

private val LOG = logger<IdeaFrameUI>()

fun IdeaFrameUI.waitForIndicatorsIgnore() {
  try {
    driver.waitForIndicators(180.seconds)
  } catch (_: Exception) {
    LOG.info("Indicators not found")
  }

}


