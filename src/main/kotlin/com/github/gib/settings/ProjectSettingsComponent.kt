package com.github.gib.settings

import com.github.gib.SettingsChangedAction
import com.github.gib.services.GivServiceSettings
import com.intellij.openapi.application.ApplicationManager
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
  private var favoritesWebListTableModel: ListTableModel<FavoritesWeb>
  private val favoritesWebTableView: TableView<FavoritesWeb>
  private var headersOverwriteListTableModel: ListTableModel<HeadersOverwrite>
  private val headersOverwriteTableView: TableView<HeadersOverwrite>
  private val givServiceSettings = GivServiceSettings.instance()
  private val homePageText = JBTextField(givServiceSettings.getLastSaveHomePage())

  init {

    val webUrl = WebUrlColumnInfo()
    favoritesWebListTableModel = ListTableModel(webUrl)
    favoritesWebTableView = TableView(favoritesWebListTableModel)

    val favoritesWebTableDecorator = ToolbarDecorator.createDecorator(favoritesWebTableView).setRemoveAction {
      removeItem(favoritesWebTableView, favoritesWebListTableModel)
    }
      .setAddAction { addWebUrl() }

    val favoritesWebScrollPanel = JBScrollPane(favoritesWebTableDecorator.createPanel())
    favoritesWebScrollPanel.preferredSize = JBUI.size(400, 200)
    favoritesWebScrollPanel.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)

    val favoritesWebTablePanel = panel {
      row {
        cell(favoritesWebScrollPanel)
          .label("Bookmarks", TOP)
          .comment("Add and Remove favorites web pages")
          .align(Align.FILL)
      }
    }.apply {
      // Border is required to have more space - otherwise there could be issues with focus ring.
      // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())

    }



    val uriRegex = UriRegexColumnInfo()
    val header = HeaderColumnInfo()
    val value = ValueColumnInfo()
    val overwrite = OverwriteColumnInfo()
    headersOverwriteListTableModel = ListTableModel(uriRegex, header, value, overwrite)
    headersOverwriteTableView = TableView(headersOverwriteListTableModel)

    val headersOverwriteTableDecorator = ToolbarDecorator
      .createDecorator(headersOverwriteTableView).setRemoveAction { removeItem(headersOverwriteTableView, headersOverwriteListTableModel) }
      .setAddAction { addHeaderOverwrite() }

    val headersOverwriteScrollPanel = JBScrollPane(headersOverwriteTableDecorator.createPanel())
    headersOverwriteScrollPanel.preferredSize = JBUI.size(400, 200)
    headersOverwriteScrollPanel.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)

    val headersOverwriteTablePanel = panel {
      row {
        cell(headersOverwriteScrollPanel)
          .label("Headers", TOP)
          .comment("Add and Remove headers. The overwrite column is used to overwrite the header if it already exists in the request.")
          .align(Align.FILL)
      }.resizableRow()
    }.apply {
      // Border is required to have more space - otherwise there could be issues with focus ring.
      // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())

    }


    myMainPanel = FormBuilder.createFormBuilder()
      .addLabeledComponent(JBLabel("GBrowser default home page"), homePageText, 1, false)
      .addComponent(favoritesWebTablePanel)
      .addComponent(headersOverwriteTablePanel)
      .addComponentFillVertically(JPanel(), 0)
      .panel
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
    addRowToTable(favoritesWebTableView, favoritesWebListTableModel, FavoritesWeb())
  }

  private fun addHeaderOverwrite() {
    addRowToTable(headersOverwriteTableView, headersOverwriteListTableModel, HeadersOverwrite("", "", "", false))
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

    return favoritesWebListTableModel.items != givServiceSettings.getFavorites() ||
           headersOverwriteListTableModel.items.map { it.uriRegex } != givServiceSettings.getHeadersOverwrite().map { it.uriRegex } ||
           headersOverwriteListTableModel.items.map { it.header } != givServiceSettings.getHeadersOverwrite().map { it.header } ||
           headersOverwriteListTableModel.items.map { it.value } != givServiceSettings.getHeadersOverwrite().map { it.value } ||
           headersOverwriteListTableModel.items.map { it.overwrite } != givServiceSettings.getHeadersOverwrite().map { it.overwrite } ||
           homePageText.text != givServiceSettings.getLastSaveHomePage()
  }

  fun apply() {
    givServiceSettings.saveHomePage(getHomePageText())
    givServiceSettings.addToFavorites(favoritesWebListTableModel.items)
    givServiceSettings.addToHeadersOverwrite(
      headersOverwriteListTableModel.items.map { HeadersOverwrite(it.uriRegex, it.header, it.value, it.overwrite) })
    val bus = ApplicationManager.getApplication().messageBus
    bus.syncPublisher(SettingsChangedAction.TOPIC).settingsChanged()
  }

  fun reset() {
    homePageText.text = givServiceSettings.getLastSaveHomePage()
    favoritesWebListTableModel.items = givServiceSettings.getFavorites()
    favoritesWebListTableModel.fireTableDataChanged()
    headersOverwriteListTableModel.items = givServiceSettings.getHeadersOverwrite().map {
      HeadersOverwrite(it.uriRegex, it.header, it.value, it.overwrite)
    }
  }

}