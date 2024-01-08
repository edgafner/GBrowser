package com.github.gbrowser.ui.search.impl

import com.github.gbrowser.ui.search.GBrowserSearchFieldDelegate
import com.github.gbrowser.ui.search.GBrowserSearchFieldPaneDelegate
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserRoundedPanel
import com.intellij.openapi.Disposable
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.util.minimumWidth
import com.intellij.util.ui.JBUI.Borders
import com.intellij.util.ui.JBUI.CurrentTheme
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import javax.swing.BoxLayout
import javax.swing.ScrollPaneConstants

class GBrowserSearchField(private val delegate: GBrowserSearchFieldDelegate) : GBrowserSearchFieldPaneDelegate, Disposable {

  private val panelColorBackground = CurrentTheme.EditorTabs.background()

  private val scrollPane: JBScrollPane by lazy {
    JBScrollPane(searchField).apply {
      isOpaque = false
      isWheelScrollingEnabled = false
      viewport.add(searchField)
      viewport.isOpaque = false
      horizontalScrollBar.setUI(GBrowserSearchFieldCustomScrollBar())
      verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
      horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
    }
  }
  private val searchField: GBrowserSearchFieldPane by lazy {
    GBrowserSearchFieldPane(this).apply {
      border = Borders.empty(5, 7, 0, 7)
      addComponentListener(object : java.awt.event.ComponentAdapter() {
        override fun componentResized(e: java.awt.event.ComponentEvent?) {
          searchField.parent?.revalidate()
          searchField.parent?.repaint()
        }
      })
    }
  }

  val component: GBrowserRoundedPanel by lazy {
    GBrowserRoundedPanel(10, panelColorBackground).apply {
      layout = BoxLayout(this, BoxLayout.Y_AXIS) //maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
      minimumSize = Dimension(50, preferredSize.height)
      add(scrollPane)
    }
  }

  var isHostHidden = true
    set(value) {
      field = value
      setLastEnteredText()
    }
  var isHostHighlighted = true
    set(value) {
      field = value
      setLastEnteredText()
    }

  private var lastText: String? = null
  private var textBeforeSelectPopupItem: String? = null


  override fun dispose() {
    searchField.dispose()
    scrollPane.removeAll()
    component.removeAll()
  }

  fun setText(value: String) {
    searchField.apply {
      isHostHighlighted = this@GBrowserSearchField.isHostHighlighted
      isHostHidden = this@GBrowserSearchField.isHostHidden
      text = value
    }
    lastText = value
  }

  private fun setLastEnteredText() {
    setText(lastText ?: "")
  }

  private fun setSelectionText(value: String) {
    if (textBeforeSelectPopupItem == null) {
      textBeforeSelectPopupItem = lastText
    }
    setText(value)
  }

  private fun resetSelectionText() {
    textBeforeSelectPopupItem?.let { setText(it) }
    textBeforeSelectPopupItem = null
  }

  fun setSearchHighlighted(isHighlighted: Boolean) {
    searchField.isSearchHighlightEnabled = isHighlighted
  }

  private fun resetScrollPosition() {
    scrollPane.viewport.viewPosition = Point(0, 0)
  }

  private fun enableScrolling(isEnabled: Boolean) {
    val policy = if (isEnabled) ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED else ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    scrollPane.horizontalScrollBarPolicy = policy
  }

  private fun enableWheelScrolling(isEnabled: Boolean) {
    scrollPane.isWheelScrollingEnabled = isEnabled
  }

  private fun hasScrolled(): Boolean {
    return scrollPane.viewport.viewPosition.x == 0
  }

  private fun selectText() {
    searchField.selectAll()
  }

  private fun deselectText() {
    searchField.deselect()
  }

  private fun setBackgroundColor(color: Color) {
    component.backgroundColor = color
  }

  override fun onEnter(url: String) {
    resetSelectionText()
    setText(url)
    enableScrolling(true)
    delegate.onEnter(url)
  }

  override fun onSelect(item: GBrowserSearchPopUpItem) {
    setSelectionText(item.url)
    resetScrollPosition()
    enableScrolling(false)
  }

  override fun onCancel() {
    resetSelectionText()
    resetScrollPosition()
    enableScrolling(true)
  }

  override fun onDoubleClick() {
    selectText()
  }

  override fun onFocus() {
    if (hasScrolled()) {
      selectText()
    }
    delegate.onFocus()
  }

  override fun onFocusLost() {
    deselectText()
    setBackgroundColor(panelColorBackground)
    resetScrollPosition()
    enableScrolling(true)
    delegate.onFocusLost()
  }

  override fun onMouseEntered() {
    setBackgroundColor(panelColorBackground)
    enableWheelScrolling(true)
  }

  override fun onMouseExited() {
    setBackgroundColor(panelColorBackground)
    enableWheelScrolling(false)
  }

  override fun onKeyReleased(text: String, popupItems: (List<GBrowserSearchPopUpItem>) -> Unit) {
    delegate.onKeyReleased(text, popupItems)
  }

}
