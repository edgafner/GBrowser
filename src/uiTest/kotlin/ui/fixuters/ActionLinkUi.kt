package ui.fixuters

import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.ui.Finder
import com.intellij.driver.sdk.ui.components.ComponentData
import com.intellij.driver.sdk.ui.components.UiComponent

fun Finder.actionLink(text: String) = x("//div[@class='ActionLink' and @text='$text']", ActionLinkUi::class.java)

class ActionLinkUi(data: ComponentData) : UiComponent(data) {
  val actionButtonComponent: ActionLinkComponent get() = driver.cast(component, ActionLinkComponent::class)
  val text: String get() = actionButtonComponent.getText()
}

@Remote("com.intellij.ui.components.ActionLink")
interface ActionLinkComponent {
  fun getText(): String
}
