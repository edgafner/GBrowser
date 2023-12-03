package com.github.gbrowser.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.ui.SearchTextField
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBImageIcon
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.event.KeyEvent
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.Border
import javax.swing.border.CompoundBorder


class GSearchFieldAction(text: String,
                         description: String,
                         icon: Icon,
                         private val jbCefBrowser: JBCefBrowser,
                         callback: (Icon) -> Unit,
                         contentCs: CoroutineScope) : AnAction(text, description, icon), CustomComponentAction {
  private val panel: JPanel = JPanel()
  private val urlTextField: SearchTextField = object : SearchTextField(true) {
    override fun preprocessEventForTextField(e: KeyEvent): Boolean {
      if (KeyEvent.VK_ENTER == e.keyCode) {
        e.consume()
        addCurrentTextToHistory()
        perform()
        val url = this.text
        contentCs.launch {
          try {
            JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=${url}")))
          }
          catch (e: Exception) {
            AllIcons.General.Web
          }.let {
            callback(it)
          }
        }
      }
      return super.preprocessEventForTextField(e)
    }


    override fun onFieldCleared() {
    }
  }

  private fun perform() {
    jbCefBrowser.loadURL(urlTextField.text)
    actionPerformed(ActionUtil.createEmptyEvent())
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    return panel
  }


  fun setText(value: String) {
    urlTextField.text = value
  }

  init {
    urlTextField.setHistorySize(10)

    val border = urlTextField.border
    val emptyBorder: Border = JBUI.Borders.empty(3, 0, 2, 0)
    if (border is CompoundBorder) {
      val lookAndFeelDefaults = UIManager.getLookAndFeelDefaults()
      val isDarkMode = lookAndFeelDefaults == null || lookAndFeelDefaults.getBoolean("ui.theme.is.dark")
      if (!isDarkMode) {
        urlTextField.border = CompoundBorder(emptyBorder, border.insideBorder)
      }
    }
    else {
      urlTextField.border = emptyBorder
    }


    panel.apply {
      isOpaque = false
      layout = MigLayout(LC().gridGap("0", "0").insets("0", "0", "0", "0"))
      panel.add(urlTextField, CC().width("280").pushX().growX())
    }
    panel.preferredSize = JBDimension(280, 32)
  }

  override fun actionPerformed(e: AnActionEvent) {

  }


}