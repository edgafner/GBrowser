package com.github.gbrowser.ui.fixtures

import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent


fun Finder.iconButton(iconName: String) = x("//div[@javaclass='com.intellij.util.ui.InlineIconButton' and @icon_delegate='${iconName}']", IconButtonUiComponent::class.java)

class IconButtonUiComponent(data: ComponentData) : UiComponent(data)