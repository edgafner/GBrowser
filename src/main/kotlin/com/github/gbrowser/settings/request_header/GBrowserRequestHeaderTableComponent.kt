package com.github.gbrowser.settings.request_header


import com.github.gbrowser.services.GBrowserService
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.SideBorder
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.TableView
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.Borders
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.UIUtil

class GBrowserRequestHeaderTableComponent(val project: Project) : Disposable {
  private val settings: GBrowserService = project.service<GBrowserService>()
  private lateinit var tableModel: ListTableModel<GBrowserRequestHeader>
  private lateinit var tableView: TableView<GBrowserRequestHeader>
  private lateinit var scrollPanel: JBScrollPane


  fun createScrollPane(): JBScrollPane {
    val columnName = GBrowserRequestHeaderColumnName()
    val columnValue = GBrowserRequestHeaderColumnValue()
    val columnOverwrite = GBrowserRequestHeaderColumnOverwrite()
    val columnRegex = GBrowserRequestHeaderUriRegexColumnInfo()
    tableModel = ListTableModel(columnRegex, columnName, columnValue, columnOverwrite)
    tableView = TableView(tableModel)
    val tableDecorator = ToolbarDecorator.createDecorator(tableView).setRemoveAction { removeAction() }.setAddAction { addAction() }
    scrollPanel = JBScrollPane(tableDecorator.createPanel()).apply {
      border = Borders.empty()
      preferredSize = JBUI.size(400, 200)
      putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)
    }
    return scrollPanel
  }

  fun isModified(): Boolean = tableModel.items != settings.requestHeaders

  fun apply() {
    settings.addRequestHeader(tableModel.items)
    tableModel.fireTableDataChanged()
  }

  fun reset() {
    tableModel.items = settings.requestHeaders.toMutableList()
    tableModel.fireTableDataChanged()
  }


  private fun removeAction() {
    val cellEditor = tableView.cellEditor
    cellEditor?.stopCellEditing()
    val item: GBrowserRequestHeader? = tableView.selectedObject
    val items = mutableListOf<GBrowserRequestHeader>()
    items.addAll(tableView.items)
    items.remove(item)
    tableModel.items = items
    tableView.requestFocusInWindow()
  }

  private fun addAction() {
    addRowToTable(GBrowserRequestHeader())
  }

  private fun addRowToTable(item: GBrowserRequestHeader) {
    val cellEditor = tableView.cellEditor
    cellEditor?.stopCellEditing()
    val items = mutableListOf<GBrowserRequestHeader>()
    items.addAll(tableView.items)
    items.add(item)
    tableModel.items = items
    val index = tableModel.rowCount - 1
    val selectionModel = tableView.selectionModel
    selectionModel.clearSelection()
    selectionModel.setSelectionInterval(index, index)
    TableUtil.scrollSelectionToVisible(tableView)

    tableView.requestFocusInWindow()
    TableUtil.editCellAt(tableView, index, 0)
    IdeFocusManager.findInstance().requestFocus(tableView.editorComponent, true)
  }

  override fun dispose() {

  }
}
