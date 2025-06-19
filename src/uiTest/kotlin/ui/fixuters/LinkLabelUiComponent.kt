package ui.fixuters

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.QueryBuilder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent


/**
 * 1) Finder extension for locating a `LinkLabel` with *exact* text:
 */
fun Finder.linkLabel(text: String): LinkLabelUiComponent = x(
  "//div[(@javaclass='com.intellij.ui.components.labels.LinkLabel' or contains(@classhierarchy,'com.intellij.ui.components.labels.LinkLabel ')) and @visible_text='$text']",
  LinkLabelUiComponent::class.java
)

/**
 * 2) Finder extension for locating a `LinkLabel` whose text *contains* some substring:
 */
fun Finder.linkLabelContains(text: String): LinkLabelUiComponent = x(
  "//div[(@javaclass='com.intellij.ui.components.labels.LinkLabel' or contains(@classhierarchy,'com.intellij.ui.components.labels.LinkLabel ')) and contains(@visible_text,'$text')]",
  LinkLabelUiComponent::class.java
)

/**
 * 3) Finder extension that accepts a QueryBuilder DSL, if needed:
 */
fun Finder.linkLabel(locator: QueryBuilder.() -> String): LinkLabelUiComponent = x(LinkLabelUiComponent::class.java) { locator() }


/**
 * A UiComponent for `com.intellij.ui.components.labels.LinkLabel`.
 */
class LinkLabelUiComponent(data: ComponentData) : UiComponent(data) {

  private val ref by lazy {
    driver.cast(component, LinkLabelRef::class)
  }

  fun getText(): String? = ref.getText()
}

/**
 * Remote interface for LinkLabel if you need direct calls.
 */
@Remote("com.intellij.ui.components.labels.LinkLabel")
interface LinkLabelRef {
  fun getText(): String? // If the LinkLabel has other interesting methods, add them here.
}