package com.github.gbrowser.ui.toolwindow.gbrowser


import java.awt.*
import javax.swing.JPanel

class GBrowserRoundedPanel(private var cornerRadius: Int = 15, private var backgroundColor: Color? = null) :
  JPanel() {

  init {
    if (backgroundColor != null) {
      isOpaque = false
    }
  }

  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
    val arcs = Dimension(cornerRadius, cornerRadius)
    val width = width
    val height = height
    val graphics = g as Graphics2D
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.color = backgroundColor ?: background
    graphics.fillRoundRect(0, 0, width, height, arcs.width, arcs.height)
  }

}
