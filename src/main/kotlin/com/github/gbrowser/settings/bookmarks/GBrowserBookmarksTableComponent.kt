package com.github.gbrowser.settings.bookmarks

import com.github.gbrowser.settings.GBrowserSetting
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.SideBorder
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.TableView
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.UIUtil


class GBrowserBookmarksTableComponent : Disposable {
  private val settings = GBrowserSetting.instance()
  private lateinit var tableView: TableView<GBrowserBookmark>
  private lateinit var tableModel: ListTableModel<GBrowserBookmark>
  private lateinit var scrollPanel: JBScrollPane


  fun createScrollPane(): JBScrollPane {
    val favoritesColumnName = GBrowserBookmarkColumnInfoName()
    val favoritesColumnUrl = GBrowserBookmarkColumnInfoUrl()
    tableModel = ListTableModel(favoritesColumnName, favoritesColumnUrl)
    tableView = TableView(tableModel)
    val tableDecorator = ToolbarDecorator.createDecorator(tableView).setRemoveAction { removeAction() }.setAddAction { addAction() }

    scrollPanel = JBScrollPane(tableDecorator.createPanel()).apply {
      preferredSize = JBUI.size(400, 200)
      border = JBUI.Borders.empty()
      putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)
    }
    return scrollPanel
  }


  fun isModified(): Boolean {
    return tableModel.items != settings.bookmarks
  }

  fun apply() {
    settings.addBookmarks(tableModel.items)
  }

  fun reset() {
    tableModel.items = settings.bookmarks.toMutableList()
    tableModel.fireTableDataChanged()
  }


  private fun removeAction() {
    val cellEditor = tableView.cellEditor
    cellEditor?.stopCellEditing()
    val item: GBrowserBookmark? = tableView.selectedObject
    val items = mutableListOf<GBrowserBookmark>()
    items.addAll(tableView.items)
    items.remove(item)
    tableModel.items = items
    tableView.requestFocusInWindow()
  }

  private fun addAction() {
    addRowToTable(GBrowserBookmark("", ""))
  }

  private fun addRowToTable(item: GBrowserBookmark) {
    tableView.cellEditor?.stopCellEditing()
    tableModel.items = mutableListOf<GBrowserBookmark>().apply {
      addAll(tableView.items + item)
    }
    val selectionModel = tableView.selectionModel
    selectionModel.clearSelection()
    val index = tableModel.rowCount - 1
    selectionModel.setSelectionInterval(index, index)
    TableUtil.scrollSelectionToVisible(tableView)
    tableView.requestFocusInWindow()
    TableUtil.editCellAt(tableView, index, 0)
    IdeFocusManager.findInstance().requestFocus(tableView.editorComponent, true)
  }

  fun validate(): ValidationInfo? {
    val invalid = tableView.items.firstOrNull {
      it.url.isEmpty()
    }
    if (invalid != null) {
      return ValidationInfo("URL cannot be empty", scrollPanel)
    }
    return null
  }

  override fun dispose() {
  }
}

