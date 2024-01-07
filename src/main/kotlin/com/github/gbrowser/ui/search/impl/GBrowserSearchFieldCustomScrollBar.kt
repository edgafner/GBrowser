package com.github.gbrowser.ui.search.impl

import com.intellij.ui.JBColor
import java.awt.*
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JScrollBar
import javax.swing.plaf.basic.BasicScrollBarUI


class GBrowserSearchFieldCustomScrollBar : BasicScrollBarUI() {
  private val buttonDimension = Dimension(12, 12)

  override fun configureScrollBarColors() {
    super.configureScrollBarColors()
    scrollBarWidth = 12
  }

  override fun createDecreaseButton(orientation: Int): JButton {
    return object : JButton() {
      private val serialVersionUID = -3592643796245558676L

      override fun getPreferredSize(): Dimension {
        return buttonDimension
      }
    }
  }

  override fun createIncreaseButton(orientation: Int): JButton {
    return object : JButton() {
      private val serialVersionUID = 1L

      override fun getPreferredSize(): Dimension {
        return buttonDimension
      }
    }
  }

  override fun paintThumb(g: Graphics, c: JComponent, r: Rectangle) {
    val g2 = g.create() as Graphics2D
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val sb = c as JScrollBar
    val color = when {
      !sb.isEnabled || r.width > r.height -> JBColor.GREEN
      isDragging -> JBColor.RED
      isThumbRollover -> JBColor.ORANGE
      else -> JBColor.BLUE
    }
    g2.paint = color
    g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 3)
  }

  override fun setThumbBounds(x: Int, y: Int, width: Int, height: Int) {
    super.setThumbBounds(x, y, width.coerceAtLeast(10), width)
    scrollbar.repaint()
  }
}
