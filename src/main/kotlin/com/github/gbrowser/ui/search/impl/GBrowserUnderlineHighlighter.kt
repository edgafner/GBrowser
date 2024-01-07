package com.github.gbrowser.ui.search.impl


import com.intellij.ui.JBColor
import java.awt.*
import javax.swing.text.*

class GBrowserUnderlineHighlighter(color: JBColor?) : DefaultHighlighter() {
  @Suppress("unused")
  var painter: Highlighter.HighlightPainter = color?.let { GBrowserUnderlineHighlightPainter(it) } ?: sharedPainter

  init {
    drawsLayeredHighlights = true
  }

  override fun setDrawsLayeredHighlights(newValue: Boolean) {
    require(newValue) { "UnderlineHighlighter only draws layered highlights" }
    super.setDrawsLayeredHighlights(true)
  }

  companion object {
    private val sharedPainter = GBrowserUnderlineHighlightPainter(null)
  }

  open class GBrowserRoundRectHighlightPainter(@Suppress("unused") private var color: JBColor?) : LayerPainter() {
    override fun paint(g: Graphics?, offs0: Int, offs1: Int, bounds: Shape?, c: JTextComponent?) { // Custom painting logic
    }

    @Suppress("DuplicatedCode")
    override fun paintLayer(g: Graphics, offs0: Int, offs1: Int, bounds: Shape, c: JTextComponent, view: View): Shape? {
      val alloc = if (offs0 == view.startOffset && offs1 == view.endOffset) {
        bounds as? Rectangle ?: bounds.bounds
      } else {
        try {
          view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds)?.let { it as? Rectangle ?: it.bounds }
        } catch (e: BadLocationException) {
          null
        }
      }

      alloc?.let {
        val arcs = Dimension(5, 5)
        g.color = JBColor(Color(g.color.red, g.color.green, g.color.blue, 60), Color(g.color.red, g.color.green, g.color.blue, 60))

        g.fillRoundRect(it.x, it.y, it.width, it.height, arcs.width, arcs.height)
        g.color = JBColor(Color(g.color.red, g.color.green, g.color.blue, 200), Color(g.color.red, g.color.green, g.color.blue, 200))
        g.drawRoundRect(it.x, it.y, it.width - 1, it.height - 1, arcs.width, arcs.height)
      }

      return alloc
    }
  }

  class GBrowserUnderlineHighlightPainter(private var color: JBColor?) : LayerPainter() {
    override fun paint(g: Graphics?, offs0: Int, offs1: Int, bounds: Shape?, c: JTextComponent?) { // Custom painting logic
    }

    @Suppress("DuplicatedCode")
    override fun paintLayer(g: Graphics, offs0: Int, offs1: Int, bounds: Shape, c: JTextComponent, view: View): Shape? {
      g.color = color ?: c.selectionColor.let { JBColor(it, it) }

      val alloc = if (offs0 == view.startOffset && offs1 == view.endOffset) {
        bounds as? Rectangle ?: bounds.bounds
      } else {
        try {
          view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds)?.let { it as? Rectangle ?: it.bounds }
        } catch (e: BadLocationException) {
          null
        }
      }

      alloc?.let {
        val fm = c.getFontMetrics(c.font)
        val baseline = it.y + it.height - fm.descent + 1
        g.color = JBColor(Color(g.color.red, g.color.green, g.color.blue, 255), Color(g.color.red, g.color.green, g.color.blue, 255))
        g.drawLine(it.x, baseline, it.x + it.width, baseline)
      }

      return alloc
    }
  }

}
