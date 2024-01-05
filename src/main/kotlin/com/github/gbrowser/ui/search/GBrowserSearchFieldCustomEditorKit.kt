package com.github.gbrowser.ui.search


import javax.swing.text.*

class GBrowserSearchFieldCustomEditorKit : StyledEditorKit() {
  companion object {
    val FACTORY: ViewFactory = ViewFactory { elem ->
      when (elem.name) {
        "component" -> ComponentView(elem)
        "icon" -> IconView(elem)
        "content" -> LabelView(elem)
        "paragraph" -> NoWrapParagraphView(elem)
        "section" -> BoxView(elem, View.Y_AXIS)
        else -> LabelView(elem)
      }
    }
  }

  override fun getViewFactory(): ViewFactory = FACTORY

  private class NoWrapParagraphView(elem: Element) : ParagraphView(elem)
}
