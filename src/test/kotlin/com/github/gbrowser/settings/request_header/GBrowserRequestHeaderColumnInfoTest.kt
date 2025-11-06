package com.github.gbrowser.settings.request_header

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserRequestHeaderColumnNameTest {

    @Test
    fun `test valueOf returns header name`() {
        val column = GBrowserRequestHeaderColumnName()
        val header = GBrowserRequestHeader("value1", "Content-Type", false, ".*")

        val value = column.valueOf(header)

        assertEquals("Content-Type", value)
    }

    @Test
    fun `test valueOf with null name returns empty string`() {
        val column = GBrowserRequestHeaderColumnName()
        val header = GBrowserRequestHeader("value1", null, false, ".*")

        val value = column.valueOf(header)

        assertEquals("", value)
    }

    @Test
    fun `test getColumnClass returns String class`() {
        val column = GBrowserRequestHeaderColumnName()

        val columnClass = column.columnClass

        assertEquals(String::class.java, columnClass)
    }

    @Test
    fun `test isCellEditable returns true`() {
        val column = GBrowserRequestHeaderColumnName()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        val isEditable = column.isCellEditable(header)

        assertTrue(isEditable, "Name column should be editable")
    }

    @Test
    fun `test setValue updates header name`() {
        val column = GBrowserRequestHeaderColumnName()
        val header = GBrowserRequestHeader("val", "OldName", false, ".*")

        column.setValue(header, "NewName")

        assertEquals("NewName", header.name)
    }

    @Test
    fun `test setValue with null sets name to null`() {
        val column = GBrowserRequestHeaderColumnName()
        val header = GBrowserRequestHeader("val", "SomeName", false, ".*")

        column.setValue(header, null)

        assertNull(header.name)
    }

    @Test
    fun `test column name is correct`() {
        val column = GBrowserRequestHeaderColumnName()

        assertEquals("Name", column.name)
    }

    @Test
    fun `test valueOf with standard HTTP header names`() {
        val column = GBrowserRequestHeaderColumnName()
        val headers = listOf("Authorization", "User-Agent", "Accept", "Cookie")

        headers.forEach { headerName ->
            val header = GBrowserRequestHeader("value", headerName, false, ".*")
            assertEquals(headerName, column.valueOf(header))
        }
    }
}

class GBrowserRequestHeaderColumnValueTest {

    @Test
    fun `test valueOf returns header value`() {
        val column = GBrowserRequestHeaderColumnValue()
        val header = GBrowserRequestHeader("Bearer token123", "Authorization", false, ".*")

        val value = column.valueOf(header)

        assertEquals("Bearer token123", value)
    }

    @Test
    fun `test valueOf with empty value`() {
        val column = GBrowserRequestHeaderColumnValue()
        val header = GBrowserRequestHeader("", "Name", false, ".*")

        val value = column.valueOf(header)

        assertEquals("", value)
    }

    @Test
    fun `test getColumnClass returns String class`() {
        val column = GBrowserRequestHeaderColumnValue()

        val columnClass = column.columnClass

        assertEquals(String::class.java, columnClass)
    }

    @Test
    fun `test isCellEditable returns true`() {
        val column = GBrowserRequestHeaderColumnValue()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        val isEditable = column.isCellEditable(header)

        assertTrue(isEditable, "Value column should be editable")
    }

    @Test
    fun `test setValue updates header value`() {
        val column = GBrowserRequestHeaderColumnValue()
        val header = GBrowserRequestHeader("old-value", "Name", false, ".*")

        column.setValue(header, "new-value")

        assertEquals("new-value", header.value)
    }

    @Test
    fun `test column name is correct`() {
        val column = GBrowserRequestHeaderColumnValue()

        assertEquals("Value", column.name)
    }

    @Test
    fun `test setValue with complex header value`() {
        val column = GBrowserRequestHeaderColumnValue()
        val header = GBrowserRequestHeader("", "Accept", false, ".*")
        val complexValue = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"

        column.setValue(header, complexValue)

        assertEquals(complexValue, header.value)
    }

    @Test
    fun `test valueOf with JSON value`() {
        val column = GBrowserRequestHeaderColumnValue()
        val jsonValue = """{"key":"value","nested":{"foo":"bar"}}"""
        val header = GBrowserRequestHeader(jsonValue, "X-Custom-Header", false, ".*")

        val value = column.valueOf(header)

        assertEquals(jsonValue, value)
    }
}

class GBrowserRequestHeaderColumnOverwriteTest {

    @Test
    fun `test valueOf returns overwrite flag`() {
        val column = GBrowserRequestHeaderColumnOverwrite()
        val header = GBrowserRequestHeader("val", "name", true, ".*")

        val value = column.valueOf(header)

        assertTrue(value)
    }

    @Test
    fun `test valueOf with false overwrite`() {
        val column = GBrowserRequestHeaderColumnOverwrite()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        val value = column.valueOf(header)

        assertFalse(value)
    }

    @Test
    fun `test getColumnClass returns Boolean class`() {
        val column = GBrowserRequestHeaderColumnOverwrite()

        val columnClass = column.columnClass

        assertEquals(Boolean::class.java, columnClass)
    }

    @Test
    fun `test isCellEditable returns true`() {
        val column = GBrowserRequestHeaderColumnOverwrite()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        val isEditable = column.isCellEditable(header)

        assertTrue(isEditable, "Overwrite column should be editable")
    }

    @Test
    fun `test setValue updates overwrite flag to true`() {
        val column = GBrowserRequestHeaderColumnOverwrite()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        column.setValue(header, true)

        assertTrue(header.overwrite)
    }

    @Test
    fun `test setValue updates overwrite flag to false`() {
        val column = GBrowserRequestHeaderColumnOverwrite()
        val header = GBrowserRequestHeader("val", "name", true, ".*")

        column.setValue(header, false)

        assertFalse(header.overwrite)
    }

    @Test
    fun `test column name is correct`() {
        val column = GBrowserRequestHeaderColumnOverwrite()

        assertEquals("Overwrite", column.name)
    }

    @Test
    fun `test toggle overwrite flag`() {
        val column = GBrowserRequestHeaderColumnOverwrite()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        // Toggle from false to true
        column.setValue(header, true)
        assertTrue(column.valueOf(header))

        // Toggle from true to false
        column.setValue(header, false)
        assertFalse(column.valueOf(header))
    }
}
