package com.github.gbrowser.ui.utils

import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.QueryBuilder
import com.intellij.driver.sdk.ui.components.common.toolwindows.StripeButtonUi
import com.intellij.driver.sdk.ui.components.elements.JButtonUiComponent
import com.intellij.driver.sdk.ui.components.elements.JLabelUiComponent
import com.intellij.driver.sdk.ui.xQuery
import org.intellij.lang.annotations.Language

fun Finder.stripeButtonT(locator: QueryBuilder.() -> String) = x(StripeButtonUi::class.java) { locator() }


fun Finder.jlabel(@Language("xpath") xpath: String? = null) = x(xpath ?: xQuery { byType(javax.swing.JLabel::class.java) }, JLabelUiComponent::class.java)


fun Finder.buttonByIcon(fileName: String) = x(JButtonUiComponent::class.java) { byAttribute("myicon", fileName) }