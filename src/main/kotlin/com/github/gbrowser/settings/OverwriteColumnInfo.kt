package com.github.gbrowser.settings

import com.intellij.util.ui.ColumnInfo
import org.jetbrains.annotations.NotNull
import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer


class OverwriteColumnInfo : ColumnInfo<GBrowserHeadersOverwrite, Boolean>("Overwrite") {

    override fun valueOf(item: GBrowserHeadersOverwrite): Boolean {
        return item.overwrite
    }

    override fun getColumnClass(): Class<*> {
        return Boolean::class.java
    }

    override fun isCellEditable(item: GBrowserHeadersOverwrite?): Boolean {
        return true
    }

    override fun setValue(item: GBrowserHeadersOverwrite, value: Boolean) {
        item.overwrite = value
    }

    override fun getRenderer(item: GBrowserHeadersOverwrite): TableCellRenderer {
        return object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                    @NotNull table: JTable,
                    value: Any,
                    isSelected: Boolean,
                    hasFocus: Boolean,
                    row: Int,
                    column: Int
            ): Component {
                val checkBox = JCheckBox()
                checkBox.isSelected = item.overwrite
                checkBox.isOpaque = true
                return checkBox
            }
        }
    }
}
