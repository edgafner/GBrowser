package com.github.gbrowser.settings

import com.intellij.util.ui.ColumnInfo
import org.jetbrains.annotations.NotNull
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer


class HeaderColumnInfo : ColumnInfo<HeadersOverwrite, String>("Header") {

    override fun valueOf(item: HeadersOverwrite): String {
        return item.header
    }

    override fun getColumnClass(): Class<*> {
        return String::class.java
    }

    override fun isCellEditable(item: HeadersOverwrite?): Boolean {
        return true
    }

    override fun setValue(item: HeadersOverwrite, value: String) {
        item.header = value
    }

    override fun getRenderer(item: HeadersOverwrite): TableCellRenderer {
        return object : DefaultTableCellRenderer() {

            override fun getTableCellRendererComponent(@NotNull table: JTable,
                                                       value: Any,
                                                       isSelected: Boolean,
                                                       hasFocus: Boolean,
                                                       row: Int,
                                                       column: Int): Component {
                val renderer: Component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column)
                border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
                text = item.header
                return renderer
            }
        }

    }
}
