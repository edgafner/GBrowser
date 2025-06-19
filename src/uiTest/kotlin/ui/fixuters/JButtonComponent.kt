package ui.fixuters

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.elements.DialogUiComponent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


fun DialogUiComponent.optionButtonByAccessName(accessibleName: String): JBOptionButtonUiComponent {
  return x(
    "//div[@class='JBOptionButton' and @accessiblename='$accessibleName']", JBOptionButtonUiComponent::class.java
  )
}

fun Finder.optionButton(text: String) = x("//div[@class='JBOptionButton' and @text='$text']", JBOptionButtonUiComponent::class.java)


fun Finder.optionButtonByAccessName(accessibleName: String): JBOptionButtonUiComponent = x(
  "//div[@class='JBOptionButton' and @accessiblename='$accessibleName']", JBOptionButtonUiComponent::class.java
)

/**
 * A UiComponent wrapper for JBOptionButton.
 */
class JBOptionButtonUiComponent(data: ComponentData) : UiComponent(data) {

  // We cast the underlying remote UI component to the JBOptionButtonRef interface
  private val optionButton by lazy {
    driver.cast(component, JBOptionButtonRef::class)
  }

  /**
   * The "text" for JBOptionButton can be analogous
   * to getText() in the underlying JButton-like API.
   */
  val text: String?
    get() = optionButton.getText()


  // Child arrow button
  // In XPaths, prefix with `.` (dot) to search relative *inside* this component’s DOM subtree.
  fun arrowButton(timeout: Duration = 5.seconds): ArrowButtonUiComponent {
    return x(".//div[@class='ArrowButton']", ArrowButtonUiComponent::class.java).waitFound(timeout)
  }

  // (Optional) convenience function
  fun clickArrowButton(timeout: Duration = 5.seconds) {
    arrowButton(timeout).click()
  }

  /*
  * This is typically a div[@class='MainButton'] if you inspect the HTML hierarchy.
  */
  fun mainButton(timeout: Duration = 5.seconds): MainButtonUiComponent {
    return x(".//div[@class='MainButton']", MainButtonUiComponent::class.java).waitFound(timeout)
  }

  fun clickMainButton(timeout: Duration = 5.seconds) {
    mainButton(timeout).click()
  }
}

/**
 * A remote reference to com.intellij.ui.components.
 * JBOptionButton
 * so we can call methods directly on the underlying Swing component.
 */
@Remote("com.intellij.ui.components.JBOptionButton")
interface JBOptionButtonRef {
  fun getText(): String?
}

/**
 * Simple UiComponent for the ArrowButton.
 * If you need advanced actions, you can cast it to a custom @Remote interface.
 */
class ArrowButtonUiComponent(data: ComponentData) : UiComponent(data)

/**
 * Simple UiComponent for the "MainButton" child of JBOptionButton.
 */
class MainButtonUiComponent(data: ComponentData) : UiComponent(data)