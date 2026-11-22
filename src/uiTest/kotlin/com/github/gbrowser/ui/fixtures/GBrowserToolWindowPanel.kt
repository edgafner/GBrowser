package com.github.gbrowser.ui.fixtures

import com.github.gbrowser.ui.utils.stripeButtonT
import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.components.common.toolwindows.ToolWindowLeftToolbarUi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Finder.showGBrowserToolWindow() = with(x(ToolWindowLeftToolbarUi::class.java) { byClass("ToolWindowLeftToolbar") }) {
  val gBrowserToolWindowButton = stripeButtonT { byAccessibleName("GBrowser") }
  gBrowserToolWindowButton.open()
}

fun Finder.gBrowserToolWindow(timeout: Duration = 20.seconds, block: GBrowserToolWindowPanel.() -> Unit) {
  showGBrowserToolWindow()
  // Match the decorator by the browser it hosts, not by its accessible name: IntelliJ names the
  // decorator after the SELECTED tab ("<page title> Tool Window"), so a tab left on e.g.
  // "GitHub · Change is constant…" made `contains(@accessiblename, 'GBrowser')` time out.
  x("//div[@class='InternalDecoratorImpl' and .//div[@class='GBrowserToolWindowBrowser']]", GBrowserToolWindowPanel::class.java).waitFound(timeout).apply(block)
}

class GBrowserToolWindowPanel(data: ComponentData) : UiComponent(data) {
  fun gBrowserPanel(timeout: Duration = 20.seconds, block: GBrowserPanel.() -> Unit) {
    x(GBrowserPanel::class.java) { byClass("GBrowserToolWindowBrowser") }.waitFound(timeout).apply(block)
  }
}

class GBrowserPanel(data: ComponentData) : UiComponent(data)