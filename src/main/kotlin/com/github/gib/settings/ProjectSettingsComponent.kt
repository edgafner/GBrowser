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
import com.intellij.ui.table.TableView
import com.intellij.util.ui.*
import javax.swing.JComponent
import javax.swing.JPanel

class ProjectSettingsComponent {
    private val myMainPanel: JPanel
    private var tableModel: ListTableModel<FavoritesWeb>
    private val table: TableView<FavoritesWeb>
    private val givServiceSettings = GivServiceSettings.instance()
    private val homePageText = JBTextField(givServiceSettings.getLastSaveHomePage())

    init {

        val webUrl = WebUrlColumnInfo()
        tableModel = ListTableModel(webUrl)
        table = TableView(tableModel)

        val tableDecorator = ToolbarDecorator.createDecorator(table).setRemoveAction { removeWebUrl() }
            .setAddAction { addWebUrl() }

        val scrollPanel = JBScrollPane(tableDecorator.createPanel())
        scrollPanel.preferredSize = JBUI.size(400, 200)
        scrollPanel.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL)

        val tablePanel =
            UI.PanelFactory.panel(scrollPanel).withLabel("Bookmarks").withComment("Add and Remove favorites web pages")
                .resizeY(true).moveLabelOnTop()
                .createPanel()
        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("GIdea default home page"), homePageText, 1, false)
            .addComponent(tablePanel)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun removeWebUrl() {
        val cellEditor = table.cellEditor
        cellEditor?.stopCellEditing()
        val item: FavoritesWeb? = table.selectedObject
        val items = mutableListOf<FavoritesWeb>()
        items.addAll(table.items)
        items.remove(item)
        tableModel.items = items
        table.requestFocusInWindow()
    }

    private fun addWebUrl() {

        val cellEditor = table.cellEditor
        cellEditor?.stopCellEditing()
        val item = FavoritesWeb("")
        val items = mutableListOf<FavoritesWeb>()
        items.addAll(table.items)
        items.add(item)
        tableModel.items = items
        val index = tableModel.rowCount - 1
        val selectionModel = table.selectionModel
        selectionModel.clearSelection()
        selectionModel.setSelectionInterval(index, index)
        TableUtil.scrollSelectionToVisible(table)

        table.requestFocusInWindow()
        TableUtil.editCellAt(table, index, 0)
        IdeFocusManager.findInstance().requestFocus(table.editorComponent, true)
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
        return tableModel.items.map { it.webUrl } != givServiceSettings.getFavorites().map { it.first } ||
            homePageText.text != givServiceSettings.getLastSaveHomePage()
    }

    fun apply() {
        givServiceSettings.saveHomePage(getHomePageText())
        givServiceSettings.addToFavorites(tableModel.items.map { it.webUrl })
        val bus = ApplicationManager.getApplication().messageBus
        bus.syncPublisher(SettingsChangedAction.TOPIC).settingsChanged()
    }

    fun reset() {
        homePageText.text = givServiceSettings.getLastSaveHomePage()
        tableModel.items = givServiceSettings.getFavorites().map { FavoritesWeb(it.first) }
        tableModel.fireTableDataChanged()
    }

}