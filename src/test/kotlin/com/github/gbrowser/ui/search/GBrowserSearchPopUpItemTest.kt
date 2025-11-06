package com.github.gbrowser.ui.search

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.swing.Icon

class GBrowserSearchPopUpItemTest {

    private val mockIcon: Icon = mockk()

    @Test
    fun `test constructor with all parameters`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "test",
            icon = mockIcon,
            info = "Test info",
            isURLVisible = true,
            name = "Test Name",
            url = "https://example.com"
        )

        assertEquals("test", item.highlight)
        assertNotNull(item.icon)
        assertEquals("Test info", item.info)
        assertTrue(item.isURLVisible)
        assertEquals("Test Name", item.name)
        assertEquals("https://example.com", item.url)
    }

    @Test
    fun `test matchesText with matching name`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertTrue(item.matchesText("git"))
        assertTrue(item.matchesText("GitHub"))
        assertTrue(item.matchesText("GITHUB"))
        assertTrue(item.matchesText("hub"))
    }

    @Test
    fun `test matchesText with matching URL`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "Example",
            url = "https://example.com"
        )

        assertTrue(item.matchesText("example"))
        assertTrue(item.matchesText("EXAMPLE"))
        assertTrue(item.matchesText(".com"))
        assertTrue(item.matchesText("https"))
    }

    @Test
    fun `test matchesText with non-matching text`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertFalse(item.matchesText("stackoverflow"))
        assertFalse(item.matchesText("xyz"))
        assertFalse(item.matchesText("random"))
    }

    @Test
    fun `test matchesText is case insensitive`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "Google",
            url = "https://google.com"
        )

        assertTrue(item.matchesText("GOOGLE"))
        assertTrue(item.matchesText("google"))
        assertTrue(item.matchesText("Google"))
        assertTrue(item.matchesText("gOoGlE"))
    }

    @Test
    fun `test matchesText with empty search text`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertTrue(item.matchesText(""), "Empty search text should match all items")
    }

    @Test
    fun `test matchesText with partial match in name`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "Documentation",
            url = "https://docs.example.com"
        )

        assertTrue(item.matchesText("doc"))
        assertTrue(item.matchesText("ment"))
        assertTrue(item.matchesText("ation"))
    }

    @Test
    fun `test matchesText with partial match in URL`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "Example",
            url = "https://api.example.com/v1/users"
        )

        assertTrue(item.matchesText("api"))
        assertTrue(item.matchesText("users"))
        assertTrue(item.matchesText("v1"))
    }

    @Test
    fun `test equals with same name and URL`() {
        val item1 = GBrowserSearchPopUpItem(
            highlight = "h1",
            icon = mockIcon,
            info = "info1",
            isURLVisible = true,
            name = "GitHub",
            url = "https://github.com"
        )

        val item2 = GBrowserSearchPopUpItem(
            highlight = "h2",
            icon = null,
            info = "info2",
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertEquals(item1, item2, "Items with same name and URL should be equal")
    }

    @Test
    fun `test equals with different names`() {
        val item1 = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        val item2 = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitLab",
            url = "https://github.com"
        )

        assertNotEquals(item1, item2, "Items with different names should not be equal")
    }

    @Test
    fun `test equals with different URLs`() {
        val item1 = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        val item2 = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://gitlab.com"
        )

        assertNotEquals(item1, item2, "Items with different URLs should not be equal")
    }

    @Test
    fun `test equals with null`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertNotEquals(item, null, "Item should not be equal to null")
    }

    @Test
    fun `test equals with different type`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertNotEquals(item, "not a search item", "Item should not be equal to different type")
    }

    @Test
    fun `test equals with same reference`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertEquals(item, item, "Item should be equal to itself")
    }

    @Test
    fun `test hashCode consistency`() {
        val item1 = GBrowserSearchPopUpItem(
            highlight = "h1",
            icon = mockIcon,
            info = "info1",
            isURLVisible = true,
            name = "GitHub",
            url = "https://github.com"
        )

        val item2 = GBrowserSearchPopUpItem(
            highlight = "h2",
            icon = null,
            info = "info2",
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        assertEquals(item1.hashCode(), item2.hashCode(), "Equal objects should have equal hash codes")
    }

    @Test
    fun `test hashCode different for different items`() {
        val item1 = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitHub",
            url = "https://github.com"
        )

        val item2 = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "GitLab",
            url = "https://gitlab.com"
        )

        assertNotEquals(item1.hashCode(), item2.hashCode(), "Different objects should likely have different hash codes")
    }

    @Test
    fun `test mutable properties can be changed`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "Original Name",
            url = "https://original.com"
        )

        val newIcon: Icon = mockk()
        item.icon = newIcon
        item.info = "New info"
        item.name = "Updated Name"
        item.url = "https://updated.com"

        assertSame(newIcon, item.icon)
        assertEquals("New info", item.info)
        assertEquals("Updated Name", item.name)
        assertEquals("https://updated.com", item.url)
    }

    @Test
    fun `test matchesText with special characters`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "API & Services",
            url = "https://example.com/api?key=value"
        )

        assertTrue(item.matchesText("api"))
        assertTrue(item.matchesText("services"))
        assertTrue(item.matchesText("key=value"))
        assertTrue(item.matchesText("&"))
    }

    @Test
    fun `test matchesText with Unicode characters`() {
        val item = GBrowserSearchPopUpItem(
            highlight = "",
            icon = null,
            info = null,
            isURLVisible = false,
            name = "日本語ページ",
            url = "https://例え.jp"
        )

        assertTrue(item.matchesText("日本"))
        assertTrue(item.matchesText("例え"))
    }
}
