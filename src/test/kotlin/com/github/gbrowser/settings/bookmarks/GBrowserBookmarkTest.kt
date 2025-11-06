package com.github.gbrowser.settings.bookmarks

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserBookmarkTest {

    @Test
    fun `test equals with same URL but different names`() {
        val bookmark1 = GBrowserBookmark("https://example.com", "Bookmark 1")
        val bookmark2 = GBrowserBookmark("https://example.com", "Bookmark 2")

        Assertions.assertEquals(bookmark1, bookmark2, "Bookmarks with same URL should be equal")
        Assertions.assertEquals(bookmark1.hashCode(), bookmark2.hashCode(), "Hash codes should be equal for equal objects")
    }

    @Test
    fun `test equals with different URLs`() {
        val bookmark1 = GBrowserBookmark("https://example1.com", "Bookmark 1")
        val bookmark2 = GBrowserBookmark("https://example2.com", "Bookmark 1")

        Assertions.assertNotEquals(bookmark1, bookmark2, "Bookmarks with different URLs should not be equal")
        Assertions.assertNotEquals(bookmark1.hashCode(), bookmark2.hashCode(), "Hash codes differ for these specific objects (note: collisions are technically possible but statistically unlikely)")
    }

    @Test
    fun `test equals with null and different type`() {
        val bookmark = GBrowserBookmark("https://example.com", "Bookmark 1")

        Assertions.assertNotEquals(bookmark, null, "Bookmark should not be equal to null")
        Assertions.assertNotEquals(bookmark, "not a bookmark object", "Bookmark should not be equal to different type")
    }

    @Test
    fun `test equals with same object reference`() {
        val bookmark = GBrowserBookmark("https://example.com", "Bookmark 1")

        Assertions.assertEquals(bookmark, bookmark, "Bookmark should be equal to itself")
    }

    @Test
    fun `test default constructor`() {
        val bookmark = GBrowserBookmark()
        
        Assertions.assertEquals("", bookmark.url, "Default URL should be empty string")
        Assertions.assertEquals("", bookmark.name, "Default name should be empty string")
    }

    @Test
    fun `test mutable properties`() {
        val bookmark = GBrowserBookmark("https://example.com", "Bookmark 1")
        
        bookmark.url = "https://updated.com"
        bookmark.name = "Updated Bookmark"
        
        Assertions.assertEquals("https://updated.com", bookmark.url, "URL should be updated")
        Assertions.assertEquals("Updated Bookmark", bookmark.name, "Name should be updated")
    }

    @Test
    fun `test serialVersionUID constant`() {
        // Just verify that the constant exists and has the expected value
        Assertions.assertEquals(12143532789876L, GBrowserBookmark.serialVersionUID)
    }
}
