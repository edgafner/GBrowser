package com.github.gbrowser.settings.request_header

import com.intellij.util.ui.ColumnInfo
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import org.jetbrains.annotations.NotNull

class GBrowserRequestHeaderColumnName : ColumnInfo<GBrowserRequestHeader, String>("Name") {

  override fun valueOf(item: GBrowserRequestHeader): String {
    return item.name ?: ""
  }

  override fun getColumnClass(): Class<String> = String::class.java

  override fun isCellEditable(item: GBrowserRequestHeader): Boolean {
    return true
  }

  override fun setValue(item: GBrowserRequestHeader, value: String?) {
    item.name = value
  }

  @NotNull
  override fun getRenderer(item: GBrowserRequestHeader): TableCellRenderer {
    @Suppress("DuplicatedCode")
    return object : DefaultTableCellRenderer() {
      override fun getTableCellRendererComponent(table: JTable,
                                                 value: Any?,
                                                 isSelected: Boolean,
                                                 hasFocus: Boolean,
                                                 row: Int,
                                                 column: Int): Component {
        val renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        this.border = BorderFactory.createEmptyBorder(5, 8, 5, 8)
        this.text = item.name
        return renderer
      }
    }
  }
}
