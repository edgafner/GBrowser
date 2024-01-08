package com.github.gbrowser.ui.search.impl


import com.github.gbrowser.ui.search.GBrowserSearchFieldPaneDelegate
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBList
import com.intellij.ui.scale.JBUIScale.scale
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.util.URI
import org.jetbrains.annotations.NonNls
import java.awt.Point
import java.awt.event.*
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.MatteBorder
import javax.swing.text.*

class GBrowserSearchFieldPane(private val delegate: GBrowserSearchFieldPaneDelegate) : JTextPane(), DataProvider, Disposable {

  var isHostHidden: Boolean = true
  var isHostHighlighted: Boolean = true
  var isSearchHighlightEnabled: Boolean = true

  private var popupSelectIndex: Int = -1
  private val popupModel = GBrowserSearchPopModel()
  private val list = JBList(popupModel)
  private val model: ListSelectionModel = list.selectionModel
  private lateinit var defaultStyle: Style
  private lateinit var grayStyle: Style
  private lateinit var portStyle: Style
  private lateinit var queryStyle: Style

  private val placeholderText = "Search or type a URL"
  private var popup: JBPopup? = null

  init {
    initComponentStyles()
    initTextStyles()
    initComponentListeners()
    initDocumentFilter()
  }

  override fun getData(dataId: @NonNls String): Any? {
    return if (PlatformDataKeys.PREDEFINED_TEXT.`is`(dataId)) text else null
  }

  override fun setText(t: String?) {
    if (t == placeholderText) {
      setColorizedPlaceholder()
    } else {
      setColorizedURL(t)
    }
  }

  override fun dispose() {
    removeComponentListeners()
    hidePopup()
    removeAll()
  }

  override fun selectAll() {
    caretPosition = document.length
    moveCaretPosition(0)
  }

  private fun initComponentStyles() {
    isOpaque = false
    margin = JBUI.emptyInsets()
    editorKit = GBrowserSearchFieldCustomEditorKit()
    border = CompoundBorder(border, MatteBorder(0, 0, 0, 0, JBColor.BLACK))
  }

  private fun initTextStyles() {
    val styleContext = StyleContext()
    defaultStyle = styleContext.addStyle("defaultStyle", null)
    defaultStyle.addAttribute(StyleConstants.Foreground, JBColor.BLACK)

    grayStyle = styleContext.addStyle("defaultStyle", null)
    grayStyle.addAttribute(StyleConstants.Foreground, JBColor.GRAY)

    portStyle = styleContext.addStyle("grayFontStyle", null)
    portStyle.addAttribute(StyleConstants.Foreground, JBColor.lightGray)

    queryStyle = styleContext.addStyle("grayFontStyle", null)
    queryStyle.addAttribute(StyleConstants.Foreground, JBColor.DARK_GRAY)
  }

  private fun initComponentListeners() {
    addFocusListener(object : FocusAdapter() {
      override fun focusGained(e: FocusEvent) {
        handlePlaceholderText()
        delegate.onFocus()
      }

      override fun focusLost(e: FocusEvent) {
        handlePlaceholderText()
        delegate.onFocusLost()
      }
    })
    addKeyListener(object : KeyAdapter() {
      override fun keyReleased(e: KeyEvent?) {
        e?.consume()
        when (e?.keyCode) {
          KeyEvent.VK_ENTER -> handleKeyEnterPress()
          KeyEvent.VK_DOWN -> handleKeyDownPress()
          KeyEvent.VK_UP -> handleKeyUpPress()
          KeyEvent.VK_ESCAPE -> handleKeyEscPress()
          else -> handleKeyPress()
        }
      }
    })
    addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent?) {
        if (e != null && e.clickCount >= 2) {
          delegate.onDoubleClick()
        }
      }

      override fun mouseEntered(e: MouseEvent?) {
        delegate.onMouseEntered()
      }

      override fun mouseExited(e: MouseEvent?) {
        if (!hasFocus()) {
          delegate.onMouseExited()
        }
      }
    })
  }

  private fun initDocumentFilter() {
    (document as? AbstractDocument)?.documentFilter = object : DocumentFilter() {
      fun existNextLine(string: String): Boolean {
        return string.contains("\n")
      }

      fun removeNextLine(line: String): String {
        return Regex("\\n").replace(line, "")
      }

      @Throws(BadLocationException::class)
      override fun replace(fb: FilterBypass, offset: Int, length: Int, string: String, attr: AttributeSet?) {
        if (existNextLine(string)) {
          fb.insertString(offset, removeNextLine(string), attr)
        } else {
          super.replace(fb, offset, length, string, attr)
        }
      }

      @Throws(BadLocationException::class)
      override fun insertString(fb: FilterBypass, offset: Int, string: String, attr: AttributeSet?) {
        if (existNextLine(string)) {
          fb.insertString(offset, removeNextLine(string), attr)
        } else {
          super.insertString(fb, offset, string, attr)
        }
      }
    }
  }


  private fun initPopUpListeners() {
    list.addListSelectionListener { e ->
      val source = e.source
      if (source is JBList<*> && source.selectedValue is GBrowserSearchPopUpItem) {
        val selectedItem = source.selectedValue as GBrowserSearchPopUpItem

        // Check if the selected item is not a separator before proceeding
        delegate.onSelect(selectedItem)
        popupSelectIndex = source.selectedIndex
      }
    }


    list.addMouseListener(object : MouseAdapter() {
      override fun mouseExited(e: MouseEvent?) {
        deselectPopupItem()
        delegate.onCancel()
      }
    })
  }

  private fun deselectPopupItem() {
    model.clearSelection()
    resetSelectionIndex()
  }


  private fun handleKeyPress() {
    delegate.onKeyReleased(text) { items ->
      popupModel.setItems(items)
      popup?.pack(true, true)

      if (popupModel.size >= 1) {
        showPopup()
      } else {
        hidePopup()
      }
    }
  }

  private fun handleKeyUpPress() {
    if (popupModel.size > 1) {
      if (popup?.isVisible == true) {
        selectPrev()
      } else {
        showPopup()
      }
    }
  }

  private fun handleKeyDownPress() {
    if (popupModel.size > 1) {
      if (popup?.isVisible == true) {
        selectNext()
      } else {
        showPopup()
      }
    }
  }

  private fun handleKeyEnterPress() {
    if (text.trim().isEmpty()) {
      clearText()
    } else {
      hidePopup()
      delegate.onEnter(text.trim())
    }
  }

  private fun handleKeyEscPress() {
    hidePopup()
  }

  private fun selectPrev() {
    if (isPrevIndexEnabled()) {
      selectPopupItem(popupSelectIndex - 1)
    } else {
      deselectPopupItem()
    }
  }

  private fun selectNext() {
    if (isNextIndexEnabled()) {
      selectPopupItem(popupSelectIndex + 1)
    } else {
      resetSelectionIndex()
    }
  }

  private fun isNextIndexEnabled(): Boolean {
    return isSelectionInRange(popupSelectIndex + 1)
  }

  private fun selectPopupItem(index: Int) {
    model.setSelectionInterval(index, index)
  }

  private fun isPrevIndexEnabled(): Boolean {
    return isSelectionInRange(popupSelectIndex - 1)
  }

  private fun isSelectionInRange(index: Int): Boolean {
    return if (0 <= index) index <= popupModel.size else false
  }

  private fun handlePlaceholderText() {
    if (text.trim().isEmpty()) {
      text = placeholderText
    } else if (text == placeholderText) {
      clearText()
    }
  }

  private fun removeComponentListeners() {
    keyListeners.forEach { removeKeyListener(it) }
    mouseListeners.forEach { removeMouseListener(it) }
    focusListeners.forEach { removeFocusListener(it) }
  }

  private fun removePopUpListeners() {
    list.listSelectionListeners.forEach { listener ->
      list.removeListSelectionListener(listener)
    }

    list.mouseListeners.forEach { listener ->
      list.removeMouseListener(listener)
    }
  }

  private fun showPopup() {
    if (!existPopUp()) {
      createPopup()
      initPopUpListeners()
    }
  }

  private fun hidePopup() {
    removePopUpListeners()
    popup?.cancel()
    popup = null
  }

  private fun existPopUp(): Boolean {
    return popup?.isVisible ?: false
  }

  fun deselect() {
    caretPosition = caretPosition
    moveCaretPosition(caretPosition)
    isFocusable = false
    isFocusable = true
  }

  private fun setColorizedPlaceholder() {
    clearText()
    styledDocument.insertString(0, placeholderText, grayStyle)
  }

  private fun setColorizedURL(t: String?) {
    clearText()
    val secondaryColor = if (isHostHighlighted) grayStyle else defaultStyle
    val value = t?.trim()

    try {
      val hasNoProtocol = value?.startsWith("http")?.not() ?: false
      @Suppress("HttpUrlsUsage") val uri = if (hasNoProtocol) URI.create("http://$value") else URI.create(value!!)

      uri.query?.let { query ->
        styledDocument.insertString(0, "?$query", secondaryColor)
      }

      uri.path?.let { path ->
        styledDocument.insertString(0, path, secondaryColor)
      }

      if (uri.port != -1) {
        styledDocument.insertString(0, uri.port.toString(), defaultStyle)
        styledDocument.insertString(0, ":", secondaryColor)
      }

      uri.host?.let { host ->
        val trimmedHost = if (host.startsWith("www.") && !isHostHidden) "www." else host.removePrefix("www.")
        styledDocument.insertString(0, trimmedHost, defaultStyle)
      }

      uri.scheme?.takeUnless { isHostHidden }?.let { scheme ->
        styledDocument.insertString(0, "$scheme://", secondaryColor)
      }
    } catch (e: Exception) {
      val url = if (isHostHidden) value?.removePrefix("www.") else value
      styledDocument.apply {
        remove(0, text.length)
        insertString(0, url, defaultStyle)
      }
    }
  }

  private fun clearText() {
    styledDocument.remove(0, text.length)
  }

  private fun createPopup() {
    val hasApplication = ApplicationManager.getApplication() != null
    if (hasApplication || isShowing) {
      val builder = PopupChooserBuilder(list).setRenderer(GBrowserSearchPopCellRenderer(isSearchHighlightEnabled)).setCloseOnEnter(
        true).setAutoSelectIfEmpty(false).setRequestFocus(false).setSelectionMode(ListSelectionModel.SINGLE_SELECTION).setAccessibleName(
        "URL List").setItemChosenCallback { item -> handleItemChosen(item) }.setCancelCallback { handlePopupCancel() }

      val position = RelativePoint(this, Point(scale(-40), scale(24)))
      val popup = builder.createPopup()
      this.popup = popup
      popup.show(position)
    }
  }


  private fun handleItemChosen(item: GBrowserSearchPopUpItem) {
    delegate.onSelect(item)
  }

  private fun handlePopupCancel(): Boolean {
    resetSelectionIndex()
    popup = null
    return true
  }

  private fun resetSelectionIndex() {
    popupSelectIndex = -1
  }


}
