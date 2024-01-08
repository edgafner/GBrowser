package com.github.gbrowser.ui.search.impl

import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserRoundedPanel
import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.CompoundBorder

class GBrowserSearchPopCellRenderer(private val highlightEnabled: Boolean) : ListCellRenderer<GBrowserSearchPopUpItem> {

  data class GBrowserSearchIndexResult(val start: Int, val end: Int)

  private fun getIndexOfText(text: String, find: String?): GBrowserSearchIndexResult {
    find?.takeUnless { it.isBlank() }?.let {
      text.indexOf(it, ignoreCase = true).takeIf { it1 -> it1 != -1 }?.let { startIndex ->
        return GBrowserSearchIndexResult(startIndex, startIndex + it.length)
      }
    }
    return GBrowserSearchIndexResult(0, 0)
  }

  override fun getListCellRendererComponent(list: JList<out GBrowserSearchPopUpItem>,
                                            value: GBrowserSearchPopUpItem,
                                            index: Int,
                                            isSelected: Boolean,
                                            cellHasFocus: Boolean): JComponent = JPanel(BorderLayout()).apply {
    isOpaque = true
    border = CompoundBorder(null, JBUI.Borders.empty(0, 8))


    val labelIcon = JLabel(value.icon ?: AllIcons.General.Web, SwingConstants.LEFT).apply {
      border = JBUI.Borders.emptyLeft(0)
      isOpaque = false
    }

    val name = value.name.let { if (it.length >= 50) it.take(50) + "..." else it }
    val labelName = createLabel(name, JBColor.BLACK, 8, value.highlight)

    @Suppress("HttpUrlsUsage") val urlValue =
      if (value.isURLVisible) value.url.removePrefix("https://").removePrefix("http://").removePrefix("www.") else ""
    val maxWidth = (60 - name.length).let { if (it <= 4) 50 else it }
    val url = if (urlValue.length >= maxWidth) urlValue.take(maxWidth) + "..." else urlValue
    val labelURL = createLabel(url, JBColor.gray, 0, value.highlight)

    val namePanel = JPanel(BorderLayout()).apply {
      isOpaque = false
      border = JBUI.Borders.empty()
      add(labelName, BorderLayout.WEST)
      add(labelURL, BorderLayout.CENTER)
    }

    val labelInfo = JBLabel(value.info ?: "").apply {
      fontColor = UIUtil.FontColor.BRIGHTER
      border = JBUI.Borders.emptyLeft(2)
      isOpaque = false
    }

    val content = GBrowserRoundedPanel(10, if (isSelected) UIUtil.getListSelectionBackground(cellHasFocus) else Color.getColor("")).apply {
      isOpaque = false
      layout = BorderLayout()
      add(labelIcon, BorderLayout.WEST)
      add(namePanel, BorderLayout.CENTER)
      add(labelInfo, BorderLayout.EAST)
      border = CompoundBorder(null, JBUI.Borders.empty(1, 7))
    }

    add(content, BorderLayout.CENTER)
  }


  private fun createLabel(text: String, color: Color, leftPadding: Int, highlight: String?): JBTextField {
    return JBTextField(text).apply {
      foreground = color
      border = JBUI.Borders.emptyLeft(leftPadding)
      isOpaque = false
      if (highlightEnabled && highlight != null) {
        getIndexOfText(text, highlight).also { indexResult ->
          highlighter.addHighlight(indexResult.start, indexResult.end,
                                   GBrowserUnderlineHighlighter.GBrowserRoundRectHighlightPainter(JBColor.BLUE))
        }
      }
    }
  }

}
