package com.github.gbrowser.settings.request_header

import com.intellij.util.ui.ColumnInfo
import org.jetbrains.annotations.NotNull
import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

class GBrowserRequestHeaderColumnOverwrite : ColumnInfo<GBrowserRequestHeader, Boolean>("Overwrite") {

  override fun valueOf(item: GBrowserRequestHeader): Boolean {
    return item.overwrite
  }

  override fun getColumnClass(): Class<Boolean> = Boolean::class.java

  override fun isCellEditable(item: GBrowserRequestHeader): Boolean {
    return true
  }

  override fun setValue(item: GBrowserRequestHeader, value: Boolean) {
    item.overwrite = value
  }


  override fun getRenderer(item: GBrowserRequestHeader): TableCellRenderer {
    return object : DefaultTableCellRenderer() {
      override fun getTableCellRendererComponent(@NotNull table: JTable,
                                                 value: Any,
                                                 isSelected: Boolean,
                                                 hasFocus: Boolean,
                                                 row: Int,
                                                 column: Int): Component {
        val checkBox = JCheckBox()
        checkBox.isSelected = item.overwrite
        checkBox.isOpaque = true
        return checkBox
      }
    }
  }
}
