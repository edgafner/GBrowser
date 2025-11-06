package com.github.gbrowser.ui.gcef.impl

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import io.mockk.mockk
import io.mockk.verify
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GBrowserCefDisplayChangeHandlerTest {

    private lateinit var mockDelegate: GBrowserToolWindowActionBarDelegate
    private lateinit var handler: GBrowserCefDisplayChangeHandler
    private lateinit var mockBrowser: CefBrowser
    private lateinit var mockFrame: CefFrame

    @BeforeEach
    fun setUp() {
        mockDelegate = mockk(relaxed = true)
        handler = GBrowserCefDisplayChangeHandler(mockDelegate)
        mockBrowser = mockk(relaxed = true)
        mockFrame = mockk(relaxed = true)
    }

    @Test
    fun `test onAddressChange with valid URL`() {
        val url = "https://example.com"

        handler.onAddressChange(mockBrowser, mockFrame, url)

        verify { mockDelegate.onAddressChange(url) }
    }

    @Test
    fun `test onAddressChange with null URL does not call delegate`() {
        handler.onAddressChange(mockBrowser, mockFrame, null)

        verify(exactly = 0) { mockDelegate.onAddressChange(any()) }
    }

    @Test
    fun `test onAddressChange with blank URL does not call delegate`() {
        handler.onAddressChange(mockBrowser, mockFrame, "")

        verify(exactly = 0) { mockDelegate.onAddressChange(any()) }
    }

    @Test
    fun `test onAddressChange with whitespace URL does not call delegate`() {
        handler.onAddressChange(mockBrowser, mockFrame, "   ")

        verify(exactly = 0) { mockDelegate.onAddressChange(any()) }
    }

    @Test
    fun `test onAddressChange with devtools URL does not call delegate`() {
        handler.onAddressChange(mockBrowser, mockFrame, "devtools://devtools/bundled/inspector.html")

        verify(exactly = 0) { mockDelegate.onAddressChange(any()) }
    }

    @Test
    fun `test onAddressChange with jbcefbrowser URL does not call delegate`() {
        handler.onAddressChange(mockBrowser, mockFrame, "file:///jbcefbrowser/page.html")

        verify(exactly = 0) { mockDelegate.onAddressChange(any()) }
    }

    @Test
    fun `test onAddressChange with http URL calls delegate`() {
        val url = "http://example.com"

        handler.onAddressChange(mockBrowser, mockFrame, url)

        verify { mockDelegate.onAddressChange(url) }
    }

    @Test
    fun `test onAddressChange with https URL calls delegate`() {
        val url = "https://secure.example.com"

        handler.onAddressChange(mockBrowser, mockFrame, url)

        verify { mockDelegate.onAddressChange(url) }
    }

    @Test
    fun `test onAddressChange with file URL calls delegate`() {
        val url = "file:///path/to/file.html"

        handler.onAddressChange(mockBrowser, mockFrame, url)

        verify { mockDelegate.onAddressChange(url) }
    }

    @Test
    fun `test onAddressChange with localhost URL calls delegate`() {
        val url = "http://localhost:8080"

        handler.onAddressChange(mockBrowser, mockFrame, url)

        verify { mockDelegate.onAddressChange(url) }
    }

    @Test
    fun `test onTitleChange with valid title`() {
        val title = "Example Page Title"

        handler.onTitleChange(mockBrowser, title)

        verify { mockDelegate.onTitleChange(title) }
    }

    @Test
    fun `test onTitleChange with null title does not call delegate`() {
        handler.onTitleChange(mockBrowser, null)

        verify(exactly = 0) { mockDelegate.onTitleChange(any()) }
    }

    @Test
    fun `test onTitleChange with blank title does not call delegate`() {
        handler.onTitleChange(mockBrowser, "")

        verify(exactly = 0) { mockDelegate.onTitleChange(any()) }
    }

    @Test
    fun `test onTitleChange with whitespace title does not call delegate`() {
        handler.onTitleChange(mockBrowser, "   ")

        verify(exactly = 0) { mockDelegate.onTitleChange(any()) }
    }

    @Test
    fun `test onTitleChange with DevTools title does not call delegate`() {
        handler.onTitleChange(mockBrowser, "DevTools - Network")

        verify(exactly = 0) { mockDelegate.onTitleChange(any()) }
    }

    @Test
    fun `test onTitleChange with title containing DevTools prefix does not call delegate`() {
        handler.onTitleChange(mockBrowser, "DevTools - Console")

        verify(exactly = 0) { mockDelegate.onTitleChange(any()) }
    }

    @Test
    fun `test onTitleChange with normal title containing devtools word calls delegate`() {
        // "devtools" in the middle of a sentence should be allowed (only "DevTools" prefix is filtered)
        val title = "How to use devtools effectively"

        handler.onTitleChange(mockBrowser, title)

        verify { mockDelegate.onTitleChange(title) }
    }

    @Test
    fun `test onTitleChange with special characters calls delegate`() {
        val title = "Page Title: Special & Characters!"

        handler.onTitleChange(mockBrowser, title)

        verify { mockDelegate.onTitleChange(title) }
    }

    @Test
    fun `test onTitleChange with Unicode characters calls delegate`() {
        val title = "例え - Example Page"

        handler.onTitleChange(mockBrowser, title)

        verify { mockDelegate.onTitleChange(title) }
    }

    @Test
    fun `test onTitleChange with very long title calls delegate`() {
        val title = "This is a very long page title that contains a lot of text " +
                "and might be used for SEO purposes or just because the page author " +
                "wanted to include a lot of information in the title tag"

        handler.onTitleChange(mockBrowser, title)

        verify { mockDelegate.onTitleChange(title) }
    }

    @Test
    fun `test multiple onAddressChange calls`() {
        val url1 = "https://example1.com"
        val url2 = "https://example2.com"
        val url3 = "https://example3.com"

        handler.onAddressChange(mockBrowser, mockFrame, url1)
        handler.onAddressChange(mockBrowser, mockFrame, url2)
        handler.onAddressChange(mockBrowser, mockFrame, url3)

        verify { mockDelegate.onAddressChange(url1) }
        verify { mockDelegate.onAddressChange(url2) }
        verify { mockDelegate.onAddressChange(url3) }
    }

    @Test
    fun `test multiple onTitleChange calls`() {
        val title1 = "Page 1"
        val title2 = "Page 2"
        val title3 = "Page 3"

        handler.onTitleChange(mockBrowser, title1)
        handler.onTitleChange(mockBrowser, title2)
        handler.onTitleChange(mockBrowser, title3)

        verify { mockDelegate.onTitleChange(title1) }
        verify { mockDelegate.onTitleChange(title2) }
        verify { mockDelegate.onTitleChange(title3) }
    }
}
