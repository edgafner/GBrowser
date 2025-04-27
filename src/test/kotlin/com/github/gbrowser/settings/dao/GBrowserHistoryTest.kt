package com.github.gbrowser.settings.dao

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserHistoryTest {

    @Test
    fun `test equals with same URL`() {
        val history1 = GBrowserHistory("Page 1", "https://example.com")
        val history2 = GBrowserHistory("Page 2", "https://example.com")
        
        Assertions.assertEquals(history1, history2, "History items with same URL should be equal")
    }
    
    @Test
    fun `test equals with different URL`() {
        val history1 = GBrowserHistory("Page", "https://example1.com")
        val history2 = GBrowserHistory("Page", "https://example2.com")
        
        Assertions.assertNotEquals(history1, history2, "History items with different URLs should not be equal")
    }
    
    @Test
    fun `test equals with null`() {
        val history = GBrowserHistory("Page", "https://example.com")
        
        Assertions.assertNotEquals(history, null, "History item should not be equal to null")
    }
    
    @Test
    fun `test equals with different object type`() {
        val history = GBrowserHistory("Page", "https://example.com")
        val otherObject = "Not a history item"
        
        Assertions.assertNotEquals(history, otherObject, "History item should not be equal to different object type")
    }
    
    @Test
    fun `test hashCode with same URL`() {
        val history1 = GBrowserHistory("Page 1", "https://example.com")
        val history2 = GBrowserHistory("Page 2", "https://example.com")
        
        Assertions.assertEquals(history1.hashCode(), history2.hashCode(), 
            "History items with same URL should have same hash code")
    }
    
    @Test
    fun `test hashCode with different URL`() {
        val history1 = GBrowserHistory("Page", "https://example1.com")
        val history2 = GBrowserHistory("Page", "https://example2.com")
        
        Assertions.assertNotEquals(history1.hashCode(), history2.hashCode(), 
            "History items with different URLs should have different hash codes")
    }
    
    @Test
    fun `test data class properties`() {
        val name = "Example Page"
        val url = "https://example.com"
        val history = GBrowserHistory(name, url)
        
        Assertions.assertEquals(name, history.name, "Name property should match constructor argument")
        Assertions.assertEquals(url, history.url, "URL property should match constructor argument")
    }
}