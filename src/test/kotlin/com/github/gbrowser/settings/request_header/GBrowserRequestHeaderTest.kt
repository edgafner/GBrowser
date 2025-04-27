package com.github.gbrowser.settings.request_header

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserRequestHeaderTest {

    @Test
    fun `test default constructor`() {
        val header = GBrowserRequestHeader()
        
        Assertions.assertEquals("", header.value, "Default value should be empty string")
        Assertions.assertNull(header.name, "Default name should be null")
        Assertions.assertFalse(header.overwrite, "Default overwrite should be false")
        Assertions.assertEquals("", header.uriRegex, "Default uriRegex should be empty string")
    }

    @Test
    fun `test constructor with parameters`() {
        val header = GBrowserRequestHeader(
            value = "test-value",
            name = "test-name",
            overwrite = true,
            uriRegex = "test-regex"
        )
        
        Assertions.assertEquals("test-value", header.value)
        Assertions.assertEquals("test-name", header.name)
        Assertions.assertTrue(header.overwrite)
        Assertions.assertEquals("test-regex", header.uriRegex)
    }

    @Test
    fun `test mutable properties`() {
        val header = GBrowserRequestHeader()
        
        header.value = "updated-value"
        header.name = "updated-name"
        header.overwrite = true
        header.uriRegex = "updated-regex"
        
        Assertions.assertEquals("updated-value", header.value)
        Assertions.assertEquals("updated-name", header.name)
        Assertions.assertTrue(header.overwrite)
        Assertions.assertEquals("updated-regex", header.uriRegex)
    }

    @Test
    fun `test equals and hashCode methods from data class`() {
        val header1 = GBrowserRequestHeader("value1", "name1", true, "regex1")
        val header2 = GBrowserRequestHeader("value1", "name1", true, "regex1")
        val header3 = GBrowserRequestHeader("value2", "name2", false, "regex2")
        
        Assertions.assertEquals(header1, header2, "Equal headers should be equal")
        Assertions.assertNotEquals(header1, header3, "Different headers should not be equal")
        
        Assertions.assertEquals(header1.hashCode(), header2.hashCode(), "Hash codes should be equal for equal objects")
        Assertions.assertNotEquals(header1.hashCode(), header3.hashCode(), "Hash codes should not be equal for unequal objects")
    }

    @Test
    fun `test serialVersionUID constant`() {
        // Just verify that the constant exists and has the expected value
        Assertions.assertEquals(3523235970041806118L, GBrowserRequestHeader.serialVersionUID)
    }
}
