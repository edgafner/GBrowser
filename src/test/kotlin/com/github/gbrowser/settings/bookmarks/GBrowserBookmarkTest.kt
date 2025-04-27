package com.github.gbrowser.settings.bookmarks

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserBookmarkTest {

    @Test
    fun `test equals with same URL`() {
        val bookmark1 = GBrowserBookmark("https://example.com", "Example 1")
        val bookmark2 = GBrowserBookmark("https://example.com", "Example 2")
        
        Assertions.assertEquals(bookmark1, bookmark2, "Bookmarks with same URL should be equal")
    }
    
    @Test
    fun `test equals with different URL`() {
        val bookmark1 = GBrowserBookmark("https://example1.com", "Example")
        val bookmark2 = GBrowserBookmark("https://example2.com", "Example")
        
        Assertions.assertNotEquals(bookmark1, bookmark2, "Bookmarks with different URLs should not be equal")
    }
    
    @Test
    fun `test equals with null`() {
        val bookmark = GBrowserBookmark("https://example.com", "Example")
        
        Assertions.assertNotEquals(bookmark, null, "Bookmark should not be equal to null")
    }
    
    @Test
    fun `test equals with different object type`() {
        val bookmark = GBrowserBookmark("https://example.com", "Example")
        val otherObject = "Not a bookmark"
        
        Assertions.assertNotEquals(bookmark, otherObject, "Bookmark should not be equal to different object type")
    }
    
    @Test
    fun `test hashCode with same URL`() {
        val bookmark1 = GBrowserBookmark("https://example.com", "Example 1")
        val bookmark2 = GBrowserBookmark("https://example.com", "Example 2")
        
        Assertions.assertEquals(bookmark1.hashCode(), bookmark2.hashCode(), 
            "Bookmarks with same URL should have same hash code")
    }
    
    @Test
    fun `test hashCode with different URL`() {
        val bookmark1 = GBrowserBookmark("https://example1.com", "Example")
        val bookmark2 = GBrowserBookmark("https://example2.com", "Example")
        
        Assertions.assertNotEquals(bookmark1.hashCode(), bookmark2.hashCode(), 
            "Bookmarks with different URLs should have different hash codes")
    }
    
    @Test
    fun `test data class properties`() {
        val url = "https://example.com"
        val name = "Example"
        val bookmark = GBrowserBookmark(url, name)
        
        Assertions.assertEquals(url, bookmark.url, "URL property should match constructor argument")
        Assertions.assertEquals(name, bookmark.name, "Name property should match constructor argument")
    }
}