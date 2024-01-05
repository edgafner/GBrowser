package com.github.gbrowser.ui.toolwindow.gbrowser


import java.awt.*
import javax.swing.JPanel

class GBrowserRoundedPanel(layout: LayoutManager? = null, private var cornerRadius: Int = 15, internal var backgroundColor: Color? = null) :
  JPanel(layout) {

  init {
    if (backgroundColor != null) {
      isOpaque = false
    }
  }

  constructor(radius: Int) : this(null, radius)

  constructor(radius: Int, bgColor: Color?) : this(null, radius, bgColor)

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

  fun setBackgroundColor(bgColor: Color?) {
    backgroundColor = bgColor
    repaint()
  }
}
