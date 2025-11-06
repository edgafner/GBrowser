package com.github.gbrowser.settings.request_header

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserRequestHeaderUriRegexColumnInfoTest {

    @Test
    fun `test valueOf returns URI regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("value", "name", false, "^https://.*")

        val value = column.valueOf(header)

        assertEquals("^https://.*", value)
    }

    @Test
    fun `test valueOf with empty regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("value", "name", false, "")

        val value = column.valueOf(header)

        assertEquals("", value)
    }

    @Test
    fun `test valueOf with match-all regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("value", "name", false, ".*")

        val value = column.valueOf(header)

        assertEquals(".*", value)
    }

    @Test
    fun `test getColumnClass returns String class`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()

        val columnClass = column.columnClass

        assertEquals(String::class.java, columnClass)
    }

    @Test
    fun `test isCellEditable returns true`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("val", "name", false, ".*")

        val isEditable = column.isCellEditable(header)

        assertTrue(isEditable, "URI regex column should be editable")
    }

    @Test
    fun `test isCellEditable with null header returns true`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()

        val isEditable = column.isCellEditable(null)

        assertTrue(isEditable, "URI regex column should be editable even with null")
    }

    @Test
    fun `test setValue updates URI regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("val", "name", false, "old-regex")

        column.setValue(header, "new-regex")

        assertEquals("new-regex", header.uriRegex)
    }

    @Test
    fun `test column name is correct`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()

        assertEquals("URI Regex", column.name)
    }

    @Test
    fun `test setValue with complex regex pattern`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("val", "name", false, "")
        val complexRegex = "^https?://(?:www\\.)?example\\.com(/.*)?$"

        column.setValue(header, complexRegex)

        assertEquals(complexRegex, header.uriRegex)
    }

    @Test
    fun `test valueOf with domain-specific regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val domainRegex = "https://api\\.example\\.com/.*"
        val header = GBrowserRequestHeader("Bearer token", "Authorization", false, domainRegex)

        val value = column.valueOf(header)

        assertEquals(domainRegex, value)
    }

    @Test
    fun `test setValue with localhost regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("val", "name", false, "")
        val localhostRegex = "http://localhost:[0-9]+/.*"

        column.setValue(header, localhostRegex)

        assertEquals(localhostRegex, header.uriRegex)
    }

    @Test
    fun `test valueOf with path-specific regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val pathRegex = ".*/api/v[0-9]+/.*"
        val header = GBrowserRequestHeader("val", "name", false, pathRegex)

        val value = column.valueOf(header)

        assertEquals(pathRegex, value)
    }

    @Test
    fun `test setValue with multiple domain regex`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("val", "name", false, "")
        val multiDomainRegex = "https://(api|cdn|www)\\.example\\.com/.*"

        column.setValue(header, multiDomainRegex)

        assertEquals(multiDomainRegex, header.uriRegex)
    }

    @Test
    fun `test setValue then valueOf round-trip`() {
        val column = GBrowserRequestHeaderUriRegexColumnInfo()
        val header = GBrowserRequestHeader("val", "name", false, "initial")
        val testRegex = "^https://secure\\.example\\.com/.*$"

        column.setValue(header, testRegex)
        val retrievedValue = column.valueOf(header)

        assertEquals(testRegex, retrievedValue)
    }
}
