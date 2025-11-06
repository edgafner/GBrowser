package com.github.gbrowser.ui.search.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserSearchIndexResultTest {

    @Test
    fun `test GBrowserSearchIndexResult creation`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(10, 20)

        assertEquals(10, result.start)
        assertEquals(20, result.end)
    }

    @Test
    fun `test GBrowserSearchIndexResult with zero indices`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(0, 0)

        assertEquals(0, result.start)
        assertEquals(0, result.end)
    }

    @Test
    fun `test GBrowserSearchIndexResult data class copy`() {
        val original = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(5, 15)
        val copy = original.copy(start = 10)

        assertEquals(10, copy.start)
        assertEquals(15, copy.end) // Unchanged
    }

    @Test
    fun `test GBrowserSearchIndexResult equals and hashCode`() {
        val result1 = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(10, 20)
        val result2 = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(10, 20)
        val result3 = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(10, 30)

        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
        assertNotEquals(result1, result3)
    }

    @Test
    fun `test GBrowserSearchIndexResult with same start and end`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(5, 5)

        assertEquals(5, result.start)
        assertEquals(5, result.end)
        assertEquals(0, result.end - result.start, "Length should be 0")
    }

    @Test
    fun `test GBrowserSearchIndexResult toString contains indices`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(10, 20)
        val toString = result.toString()

        assertTrue(toString.contains("10"))
        assertTrue(toString.contains("20"))
    }

    @Test
    fun `test GBrowserSearchIndexResult with large indices`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(1000, 2000)

        assertEquals(1000, result.start)
        assertEquals(2000, result.end)
        assertEquals(1000, result.end - result.start, "Length should be 1000")
    }

    @Test
    fun `test GBrowserSearchIndexResult represents a valid range`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(50, 100)

        assertTrue(result.end >= result.start, "End should be greater than or equal to start")
    }

    @Test
    fun `test GBrowserSearchIndexResult with negative values`() {
        // While not typical, test that the data class can hold negative values
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(-1, -1)

        assertEquals(-1, result.start)
        assertEquals(-1, result.end)
    }

    @Test
    fun `test GBrowserSearchIndexResult component accessors`() {
        val result = GBrowserSearchPopCellRenderer.GBrowserSearchIndexResult(15, 25)

        // Test destructuring (data class component functions)
        val (start, end) = result
        assertEquals(15, start)
        assertEquals(25, end)
    }
}
