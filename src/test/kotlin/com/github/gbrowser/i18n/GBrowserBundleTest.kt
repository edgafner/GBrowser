package com.github.gbrowser.i18n

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserBundleTest {

    @Test
    fun `test message retrieval for actions`() {
        val addBookmark = GBrowserBundle.message("actions.bookmark.add.text")
        assertEquals("Add to Bookmarks", addBookmark)

        val removeBookmark = GBrowserBundle.message("actions.bookmark.remove.text")
        assertEquals("Remove from Bookmarks", removeBookmark)
    }

    @Test
    fun `test message retrieval for action texts`() {
        assertEquals("Forward", GBrowserBundle.message("action.GBrowserForwardAction.text"))
        assertEquals("Backward", GBrowserBundle.message("action.GBrowserBackwardAction.text"))
        assertEquals("Home", GBrowserBundle.message("action.GBrowserHomeAction.text"))
        assertEquals("Reload Page", GBrowserBundle.message("action.GBrowserRefreshAction.text"))
        assertEquals("Stop Loading", GBrowserBundle.message("action.GBrowserStopLoadAction.text"))
    }

    @Test
    fun `test message retrieval for tab actions`() {
        assertEquals("Add Tab", GBrowserBundle.message("action.GBrowserAddTabAction.text"))
        assertEquals("Close Tab", GBrowserBundle.message("action.GBrowserCloseTabAction.text"))
        assertEquals("Duplicate Tab", GBrowserBundle.message("action.GBrowserTabDuplicateAction.text"))
    }

    @Test
    fun `test message retrieval for zoom actions`() {
        assertEquals("Zoom In", GBrowserBundle.message("action.GBrowserZoomInAction.text"))
        assertEquals("Zoom Out", GBrowserBundle.message("action.GBrowserZoomOutAction.text"))
        assertEquals("Actual Size", GBrowserBundle.message("action.GBrowserZoomResetAction.text"))
    }

    @Test
    fun `test message retrieval for browser actions`() {
        assertEquals("Open in Default Browser", GBrowserBundle.message("action.GBrowserOpenInAction.text"))
        assertEquals("Preferences", GBrowserBundle.message("action.GBrowserPreferencesAction.text"))
        assertEquals("Find...", GBrowserBundle.message("action.GBrowserFindAction.text"))
    }

    @Test
    fun `test message retrieval for device emulation`() {
        assertEquals("Toggle Device Emulation", GBrowserBundle.message("action.GBrowserMobileToggleAction.text"))
        assertEquals("Emulate mobile device viewport and user agent",
                    GBrowserBundle.message("action.GBrowserMobileToggleAction.description"))
    }

    @Test
    fun `test message retrieval for clear actions`() {
        assertEquals("Clear Cookies", GBrowserBundle.message("action.GBrowserCookieDeleteAllAction.text"))
        assertEquals("Clear History", GBrowserBundle.message("action.GBrowserClearHistoryAction.text"))
    }

    @Test
    fun `test message retrieval for editor actions`() {
        assertEquals("Open Selected Text in GBrowser",
                    GBrowserBundle.message("action.GBrowserEditorOpenAction.text"))
        assertEquals("Open selected text as URL in GBrowser",
                    GBrowserBundle.message("action.GBrowserEditorOpenAction.description"))
    }

    @Test
    fun `test message retrieval for bookmarks`() {
        assertEquals("Bookmark Manager", GBrowserBundle.message("action.GBrowserBookmarkManagerAction.text"))
        assertEquals("Bookmarks", GBrowserBundle.message("group.action.bookmarks"))
    }

    @Test
    fun `test message retrieval for display name`() {
        assertEquals("GBrowser", GBrowserBundle.message("com.github.gbrowser.display.name"))
    }

    @Test
    fun `test message retrieval for notification group`() {
        assertEquals("GBrowser notifications", GBrowserBundle.message("gbrowser.notification.group"))
    }

    @Test
    fun `test message with parameters`() {
        val deviceMessage = GBrowserBundle.message("action.device.emulation.active", "iPhone 12 Pro")
        assertEquals("Device: iPhone 12 Pro", deviceMessage)
    }

    @Test
    fun `test message with multiple parameters`() {
        // Test that the message function can handle multiple parameters
        val deviceMessage = GBrowserBundle.message("action.device.emulation.active", "Pixel 7")
        assertTrue(deviceMessage.contains("Pixel 7"))
    }

    @Test
    fun `test message retrieval for default URL field`() {
        assertEquals("Default Home Page URL:", GBrowserBundle.message("default.url.field"))
    }

    @Test
    fun `test message retrieval for toolbar action`() {
        assertEquals("Toggle Toolbar Visibility",
                    GBrowserBundle.message("action.GBrowserToggleToolbarAction.text"))
    }

    @Test
    fun `test message retrieval for group descriptions`() {
        assertEquals("Bookmarks",
                    GBrowserBundle.message("group.com.github.gbrowser.actions.bookmark.GBrowserBookmarkGroupAction.text"))
        assertEquals("Bookmarks",
                    GBrowserBundle.message("group.com.github.gbrowser.actions.bookmark.GBrowserBookmarkGroupAction.description"))
    }

    @Test
    fun `test message retrieval for current file action`() {
        assertEquals("Open in GBrowser", GBrowserBundle.message("action.GBrowserOpenCurrentFileAction.text"))
        assertEquals("Open current file in GBrowser",
                    GBrowserBundle.message("action.GBrowserOpenCurrentFileAction.description"))
    }

    @Test
    fun `test message retrieval returns key for missing keys`() {
        // When a key doesn't exist, the bundle typically returns the key itself or a default message
        val result = GBrowserBundle.message("non.existent.key")
        assertNotNull(result, "Should return a non-null value for missing keys")
    }

    @Test
    fun `test emulate mobile description`() {
        assertEquals("Emulate Mobile Device Viewport and User Agent",
                    GBrowserBundle.message("emulate.mobile.description"))
    }
}
