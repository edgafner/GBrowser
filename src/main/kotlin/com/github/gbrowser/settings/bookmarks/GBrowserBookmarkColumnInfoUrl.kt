package com.github.gbrowser.settings.bookmarks


import com.intellij.util.ui.ColumnInfo
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

class GBrowserBookmarkColumnInfoUrl : ColumnInfo<GBrowserBookmark, String>("URL") {

  override fun valueOf(item: GBrowserBookmark): String {
    return item.url
  }

  override fun getColumnClass(): Class<*> {
    return String::class.java
  }

  override fun isCellEditable(item: GBrowserBookmark?): Boolean {
    return true
  }

  override fun setValue(item: GBrowserBookmark, value: String) {
    item.url = value
  }

  @Suppress("DuplicatedCode")
  override fun getRenderer(item: GBrowserBookmark): TableCellRenderer {
    return object : DefaultTableCellRenderer() {
      override fun getTableCellRendererComponent(table: JTable,
                                                 value: Any,
                                                 isSelected: Boolean,
                                                 hasFocus: Boolean,
                                                 row: Int,
                                                 column: Int): Component {
        val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        border = BorderFactory.createEmptyBorder(5, 8, 5, 8)
        text = item.url
        return renderer
      }
    }
  }
}
