package com.github.gib

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefCookie
import com.intellij.ui.jcef.JBCefCookieManager
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import javax.swing.*
import javax.swing.table.AbstractTableModel

@Suppress("UnstableApiUsage")
class GCookieManagerDialog(parent: JPanel, val jbCefBrowser: JBCefBrowser) : DialogWrapper(parent, false) {
    private val myDeleteCookiesButtonText = "Delete All Cookies"
    private val myJBCefCookieManager: JBCefCookieManager = jbCefBrowser.jbCefCookieManager
    private val myTableModel = CookieTableModel()

    init {
        title = "Cookie Manager"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            val cookieTable: JTable = JBTable(myTableModel)
            cookieTable.fillsViewportHeight = true
            val component = JScrollPane(cookieTable)
            component.preferredSize = JBUI.size(500, 300)
            cell(component).horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)

        }.bottomGap(BottomGap.MEDIUM)
        separator()
        row {
            button(myDeleteCookiesButtonText) {
                if (myJBCefCookieManager.deleteCookies(true)) {
                    val cookies = myJBCefCookieManager.cookies
                    cookies?.let { update(it) }
                }
            }
        }
    }.withBorder(JBUI.Borders.empty(5)).withPreferredSize(600, 400)


    /**
     * This to disable the ok and cancel
     */
    override fun createActions(): Array<Action> {
        return emptyArray()
    }


    fun update(cefCookies: List<JBCefCookie>) {
        myTableModel.clear()
        myTableModel.show(cefCookies)
    }

    class CookieTableModel : AbstractTableModel() {
        private val columnNames =
            arrayOf("Name", "Value", "Domain", "Path", "Secure", "HTTP only", "Created", "Last Access", "Expires")
        private val rowData = ArrayList<Array<Any>>()

        fun show(cefCookies: List<JBCefCookie>) {
            for (cookie in cefCookies) {
                val entry = arrayOf<Any>(cookie.name,
                    cookie.value,
                    cookie.domain,
                    cookie.path,
                    cookie.isSecure,
                    cookie.isHttpOnly)
                val row = rowData.size
                rowData.add(entry)
                fireTableRowsInserted(row, row)
            }
        }

        fun clear() {
            val count = rowData.size
            if (count > 0) {
                rowData.clear()
                fireTableRowsDeleted(0, count - 1)
            }
        }

        override fun getRowCount(): Int {
            return rowData.size
        }

        override fun getColumnCount(): Int {
            return columnNames.size
        }

        override fun getColumnName(column: Int): String {
            return columnNames[column]
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            if (rowData.size > 0) {
                return rowData[0][columnIndex].javaClass
            }
            return Any::class.java
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            return false
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return rowData[rowIndex][columnIndex]
        }
    }


}


