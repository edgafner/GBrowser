package com.github.gbrowser.settings

import com.github.gbrowser.SettingsChangedAction
import com.github.gbrowser.services.GBrowserSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.SideBorder
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.LabelPosition.TOP
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.*
import javax.swing.JComponent
import javax.swing.JPanel

class ProjectSettingsComponent {
  private val myMainPanel: JPanel

  private var bookmarksListTableModel: ListTableModel<GBrowserBookmarks>
  private val bookmarksTableView: TableView<GBrowserBookmarks>

  private var quickAccessBookmarksListTableModel: ListTableModel<GBrowserBookmarks>
  private val quickAccessBookmarksTableView: TableView<GBrowserBookmarks>

  private var headersOverwriteListTableModel: ListTableModel<GBrowserHeadersOverwrite>
  private val headersOverwriteTableView: TableView<GBrowserHeadersOverwrite>

  private val gbrowserSettings = GBrowserSettings.instance()
  private val homePageText = JBTextField(gbrowserSettings.getHomePage())

  init {

    val webUrl = WebUrlColumnInfo()
    bookmarksListTableModel = ListTableModel(webUrl)
    bookmarksTableView = TableView(bookmarksListTableModel)
    val bookMarksWebTablePanel = getBookMarksPanel()


    quickAccessBookmarksListTableModel = ListTableModel(webUrl)
    quickAccessBookmarksTableView = TableView(quickAccessBookmarksListTableModel)
    val quickAccessWebTablePanel = getQuickAccessBookMarksPanel()

    val uriRegex = UriRegexColumnInfo()
    val header = HeaderColumnInfo()
    val value = ValueColumnInfo()
    val overwrite = OverwriteColumnInfo()
    headersOverwriteListTableModel = ListTableModel(uriRegex, header, value, overwrite)
    headersOverwriteTableView = TableView(headersOverwriteListTableModel)
    val headersOverwriteTablePanel = getHeaderOverwritePanel()


    myMainPanel = FormBuilder.createFormBuilder().addLabeledComponent(JBLabel("GBrowser home page"), homePageText, 1, false).addComponent(
      bookMarksWebTablePanel).addComponent(quickAccessWebTablePanel).addComponent(headersOverwriteTablePanel).addComponentFillVertically(
      JPanel(), 0).panel
  }

  @Suppress("DuplicatedCode")
  private fun getBookMarksPanel(): DialogPanel {
    val bookMarksWebTableDecorator = ToolbarDecorator.createDecorator(bookmarksTableView).setRemoveAction {
      removeItem(bookmarksTableView, bookmarksListTableModel)
    }.setAddAction { addWebUrl() }

    val bookMarksWebScrollPanel = JBScrollPane(bookMarksWebTableDecorator.createPanel())
    bookMarksWebScrollPanel.preferredSize = JBUI.size(400, 150)
    bookMarksWebScrollPanel.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)
    val bookMarksWebTablePanel = panel {
      row {
        cell(bookMarksWebScrollPanel).label("Bookmarks", TOP).comment("Add and Remove bookmarks web pages").align(Align.FILL)
      }
    }.apply {

      // Border is required to have more space - otherwise there could be issues with focus ring.
      // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())

    }
    return bookMarksWebTablePanel
  }

  @Suppress("DuplicatedCode")
  private fun getQuickAccessBookMarksPanel(): DialogPanel {
    val quickAccessBookMarksWebTableDecorator = ToolbarDecorator.createDecorator(quickAccessBookmarksTableView).setRemoveAction {
      removeItem(quickAccessBookmarksTableView, quickAccessBookmarksListTableModel)
    }.setAddAction { addQuickAccessWebUrl() }

    val bookMarksWebScrollPanel = JBScrollPane(quickAccessBookMarksWebTableDecorator.createPanel())
    bookMarksWebScrollPanel.preferredSize = JBUI.size(400, 150)
    bookMarksWebScrollPanel.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)
    val quickAccessBookMarksWebTablePanel = panel {
      row {
        cell(bookMarksWebScrollPanel).label("Quick access", TOP).comment("Add and Remove quick access from options").align(Align.FILL)
      }
    }.apply {

      // Border is required to have more space - otherwise there could be issues with focus ring.
      // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())

    }
    return quickAccessBookMarksWebTablePanel
  }

  private fun getHeaderOverwritePanel(): DialogPanel {
    val headersOverwriteTableDecorator = ToolbarDecorator.createDecorator(headersOverwriteTableView).setRemoveAction {
      removeItem(headersOverwriteTableView, headersOverwriteListTableModel)
    }.setAddAction { addHeaderOverwrite() }

    val headersOverwriteScrollPanel = JBScrollPane(headersOverwriteTableDecorator.createPanel())
    headersOverwriteScrollPanel.preferredSize = JBUI.size(400, 150)
    headersOverwriteScrollPanel.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)
    val headersOverwriteTablePanel = panel {
      row {
        cell(headersOverwriteScrollPanel).label("Headers", TOP).comment(
          "Add and Remove headers. The overwrite column is used to overwrite the header if it already exists in the request.").align(
          Align.FILL)
      }.resizableRow()
    }.apply { // Border is required to have more space - otherwise there could be issues with focus ring.
      // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())

    }
    return headersOverwriteTablePanel
  }

  private fun <T> removeItem(tableView: TableView<T>, listTableModel: ListTableModel<T>) {
    val cellEditor = tableView.cellEditor
    cellEditor?.stopCellEditing()
    val item: T? = tableView.selectedObject
    val items = mutableListOf<T>()
    items.addAll(tableView.items)
    items.remove(item)
    listTableModel.items = items
    tableView.requestFocusInWindow()
  }

  private fun <T> addRowToTable(tableView: TableView<T>, tableModel: ListTableModel<T>, item: T) {
    val cellEditor = tableView.cellEditor
    cellEditor?.stopCellEditing()
    val items = mutableListOf<T>()
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

  private fun addWebUrl() {
    addRowToTable(bookmarksTableView, bookmarksListTableModel, GBrowserBookmarks())
  }

  private fun addQuickAccessWebUrl() {
    addRowToTable(quickAccessBookmarksTableView, quickAccessBookmarksListTableModel, GBrowserBookmarks())
  }

  private fun addHeaderOverwrite() {
    addRowToTable(headersOverwriteTableView, headersOverwriteListTableModel, GBrowserHeadersOverwrite("", "", "", false))
  }

  fun getPanel(): JPanel {
    return myMainPanel
  }

  fun getPreferredFocusedComponent(): JComponent {
    return homePageText
  }

  private fun getHomePageText(): String {
    return homePageText.text
  }

  fun isModified(): Boolean {

    val uirRegexChanged = headersOverwriteListTableModel.items.map { it.uriRegex } != gbrowserSettings.getHeadersOverwrite().map { it.uriRegex }
    val headerChanged = headersOverwriteListTableModel.items.map { it.header } != gbrowserSettings.getHeadersOverwrite().map { it.header }
    val valueChanged = headersOverwriteListTableModel.items.map { it.value } != gbrowserSettings.getHeadersOverwrite().map { it.value }
    val overWriteValueChanged = headersOverwriteListTableModel.items.map { it.overwrite } != gbrowserSettings.getHeadersOverwrite().map { it.overwrite }
    return bookmarksListTableModel.items != gbrowserSettings.getBookmarks() || quickAccessBookmarksListTableModel.items != gbrowserSettings.getQuickAccessBookmarks() || uirRegexChanged || headerChanged || valueChanged || overWriteValueChanged || homePageText.text != gbrowserSettings.getHomePage()
  }

  fun apply() {
    gbrowserSettings.saveHomePage(getHomePageText())
    gbrowserSettings.addToBookmarks(bookmarksListTableModel.items)
    gbrowserSettings.addToQuickAccessBookmarks(quickAccessBookmarksListTableModel.items)
    gbrowserSettings.addToHeadersOverwrite(
      headersOverwriteListTableModel.items.map { GBrowserHeadersOverwrite(it.uriRegex, it.header, it.value, it.overwrite) })
    val bus = ApplicationManager.getApplication().messageBus
    bus.syncPublisher(SettingsChangedAction.TOPIC).settingsChanged()
  }

  fun reset() {
    homePageText.text = gbrowserSettings.getHomePage()
    bookmarksListTableModel.items = gbrowserSettings.getBookmarks()
    bookmarksListTableModel.fireTableDataChanged()
    quickAccessBookmarksListTableModel.items = gbrowserSettings.getQuickAccessBookmarks()
    quickAccessBookmarksListTableModel.fireTableDataChanged()

    headersOverwriteListTableModel.items = gbrowserSettings.getHeadersOverwrite().map {
      GBrowserHeadersOverwrite(it.uriRegex, it.header, it.value, it.overwrite)
    }
  }

}