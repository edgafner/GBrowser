package com.github.gbrowser.settings.request_header

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserRequestHeaderTest {

    @Test
    fun `test data class properties`() {
        val value = "test-value"
        val name = "test-name"
        val overwrite = true
        val uriRegex = "test-regex"
        
        val header = GBrowserRequestHeader(value, name, overwrite, uriRegex)
        
        Assertions.assertEquals(value, header.value, "Value property should match constructor argument")
        Assertions.assertEquals(name, header.name, "Name property should match constructor argument")
        Assertions.assertEquals(overwrite, header.overwrite, "Overwrite property should match constructor argument")
        Assertions.assertEquals(uriRegex, header.uriRegex, "UriRegex property should match constructor argument")
    }
    
    @Test
    fun `test data class default values`() {
        val header = GBrowserRequestHeader()
        
        Assertions.assertEquals("", header.value, "Default value should be empty string")
        Assertions.assertNull(header.name, "Default name should be null")
        Assertions.assertFalse(header.overwrite, "Default overwrite should be false")
        Assertions.assertEquals("", header.uriRegex, "Default uriRegex should be empty string")
    }
    
    @Test
    fun `test data class copy`() {
        val original = GBrowserRequestHeader("original-value", "original-name", true, "original-regex")
        val copy = original.copy(value = "new-value")
        
        Assertions.assertEquals("new-value", copy.value, "Copied value should be updated")
        Assertions.assertEquals(original.name, copy.name, "Copied name should remain the same")
        Assertions.assertEquals(original.overwrite, copy.overwrite, "Copied overwrite should remain the same")
        Assertions.assertEquals(original.uriRegex, copy.uriRegex, "Copied uriRegex should remain the same")
    }
    
    @Test
    fun `test data class equals`() {
        val header1 = GBrowserRequestHeader("value", "name", true, "regex")
        val header2 = GBrowserRequestHeader("value", "name", true, "regex")
        val header3 = GBrowserRequestHeader("different", "name", true, "regex")
        
        Assertions.assertEquals(header1, header2, "Headers with same properties should be equal")
        Assertions.assertNotEquals(header1, header3, "Headers with different properties should not be equal")
    }
    
    @Test
    fun `test data class hashCode`() {
        val header1 = GBrowserRequestHeader("value", "name", true, "regex")
        val header2 = GBrowserRequestHeader("value", "name", true, "regex")
        val header3 = GBrowserRequestHeader("different", "name", true, "regex")
        
        Assertions.assertEquals(header1.hashCode(), header2.hashCode(), 
            "Headers with same properties should have same hash code")
        Assertions.assertNotEquals(header1.hashCode(), header3.hashCode(), 
            "Headers with different properties should have different hash codes")
    }
}