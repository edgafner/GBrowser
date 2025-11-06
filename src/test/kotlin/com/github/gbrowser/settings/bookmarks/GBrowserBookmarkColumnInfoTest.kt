package com.github.gbrowser.settings.bookmarks

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserBookmarkColumnInfoNameTest {

    @Test
    fun `test valueOf returns bookmark name`() {
        val column = GBrowserBookmarkColumnInfoName()
        val bookmark = GBrowserBookmark("https://example.com", "Example Site")

        val value = column.valueOf(bookmark)

        assertEquals("Example Site", value)
    }

    @Test
    fun `test valueOf with empty name`() {
        val column = GBrowserBookmarkColumnInfoName()
        val bookmark = GBrowserBookmark("https://example.com", "")

        val value = column.valueOf(bookmark)

        assertEquals("", value)
    }

    @Test
    fun `test getColumnClass returns String class`() {
        val column = GBrowserBookmarkColumnInfoName()

        val columnClass = column.columnClass

        assertEquals(String::class.java, columnClass)
    }

    @Test
    fun `test isCellEditable returns true`() {
        val column = GBrowserBookmarkColumnInfoName()
        val bookmark = GBrowserBookmark("https://example.com", "Example")

        val isEditable = column.isCellEditable(bookmark)

        assertTrue(isEditable, "Name column should be editable")
    }

    @Test
    fun `test setValue updates bookmark name`() {
        val column = GBrowserBookmarkColumnInfoName()
        val bookmark = GBrowserBookmark("https://example.com", "Old Name")

        column.setValue(bookmark, "New Name")

        assertEquals("New Name", bookmark.name)
    }

    @Test
    fun `test setValue with empty string`() {
        val column = GBrowserBookmarkColumnInfoName()
        val bookmark = GBrowserBookmark("https://example.com", "Original")

        column.setValue(bookmark, "")

        assertEquals("", bookmark.name)
    }

    @Test
    fun `test column name is correct`() {
        val column = GBrowserBookmarkColumnInfoName()

        assertEquals("Name", column.name)
    }
}

class GBrowserBookmarkColumnInfoUrlTest {

    @Test
    fun `test valueOf returns bookmark URL`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val bookmark = GBrowserBookmark("https://example.com", "Example Site")

        val value = column.valueOf(bookmark)

        assertEquals("https://example.com", value)
    }

    @Test
    fun `test valueOf with empty URL`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val bookmark = GBrowserBookmark("", "Example Site")

        val value = column.valueOf(bookmark)

        assertEquals("", value)
    }

    @Test
    fun `test getColumnClass returns String class`() {
        val column = GBrowserBookmarkColumnInfoUrl()

        val columnClass = column.columnClass

        assertEquals(String::class.java, columnClass)
    }

    @Test
    fun `test isCellEditable returns true`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val bookmark = GBrowserBookmark("https://example.com", "Example")

        val isEditable = column.isCellEditable(bookmark)

        assertTrue(isEditable, "URL column should be editable")
    }

    @Test
    fun `test isCellEditable with null bookmark returns true`() {
        val column = GBrowserBookmarkColumnInfoUrl()

        val isEditable = column.isCellEditable(null)

        assertTrue(isEditable, "URL column should be editable even with null")
    }

    @Test
    fun `test setValue updates bookmark URL`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val bookmark = GBrowserBookmark("https://old.com", "Site")

        column.setValue(bookmark, "https://new.com")

        assertEquals("https://new.com", bookmark.url)
    }

    @Test
    fun `test setValue with protocol-less URL`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val bookmark = GBrowserBookmark("https://example.com", "Site")

        column.setValue(bookmark, "example.org")

        assertEquals("example.org", bookmark.url)
    }

    @Test
    fun `test column name is correct`() {
        val column = GBrowserBookmarkColumnInfoUrl()

        assertEquals("URL", column.name)
    }

    @Test
    fun `test setValue with localhost URL`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val bookmark = GBrowserBookmark("", "Local")

        column.setValue(bookmark, "http://localhost:8080")

        assertEquals("http://localhost:8080", bookmark.url)
    }

    @Test
    fun `test valueOf with complex URL`() {
        val column = GBrowserBookmarkColumnInfoUrl()
        val complexUrl = "https://example.com/path?query=value&foo=bar#fragment"
        val bookmark = GBrowserBookmark(complexUrl, "Complex")

        val value = column.valueOf(bookmark)

        assertEquals(complexUrl, value)
    }
}
