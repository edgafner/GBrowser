package com.github.gbrowser.ui.gcef

import org.cef.handler.CefLoadHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GBrowserErrorPageTest {

    @Test
    fun `test error page creation with valid parameters`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_NAME_NOT_RESOLVED
        val errorText = "DNS resolution failed"
        val failedUrl = "https://invalid-domain-test.com"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertNotNull(errorPage, "Error page should not be null")
        assertTrue(errorPage.isNotEmpty(), "Error page should not be empty")
    }

    @Test
    fun `test error page contains error text`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_CONNECTION_REFUSED
        val errorText = "Connection refused by server"
        val failedUrl = "https://example.com"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertTrue(errorPage.contains(errorText), "Error page should contain the error text")
    }

    @Test
    fun `test error page contains failed URL`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_CONNECTION_TIMED_OUT
        val errorText = "Connection timed out"
        val failedUrl = "https://slow-server.example.com"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertTrue(errorPage.contains(failedUrl), "Error page should contain the failed URL")
    }

    @Test
    fun `test error page is valid HTML`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_FAILED
        val errorText = "Generic error"
        val failedUrl = "https://example.com"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        // Check for basic HTML structure
        assertTrue(errorPage.contains("<html"), "Error page should contain HTML opening tag")
        assertTrue(errorPage.contains("</html>"), "Error page should contain HTML closing tag")
        assertTrue(errorPage.contains("<head"), "Error page should contain head tag")
        assertTrue(errorPage.contains("<body"), "Error page should contain body tag")
    }

    @Test
    fun `test error page handles special characters in URL`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_INVALID_URL
        val errorText = "Invalid URL"
        val failedUrl = "https://example.com/path?query=value&special=<>&\""

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertNotNull(errorPage)
        assertTrue(errorPage.isNotEmpty(), "Error page should handle special characters")
    }

    @Test
    fun `test error page handles empty error text`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_FAILED
        val errorText = ""
        val failedUrl = "https://example.com"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertNotNull(errorPage)
        assertTrue(errorPage.isNotEmpty(), "Error page should be generated even with empty error text")
    }

    @Test
    fun `test error page handles empty URL`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_FAILED
        val errorText = "Error occurred"
        val failedUrl = ""

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertNotNull(errorPage)
        assertTrue(errorPage.isNotEmpty(), "Error page should be generated even with empty URL")
    }

    @Test
    fun `test error page with different error codes`() {
        val errorCodes = listOf(
            CefLoadHandler.ErrorCode.ERR_CONNECTION_REFUSED,
            CefLoadHandler.ErrorCode.ERR_NAME_NOT_RESOLVED,
            CefLoadHandler.ErrorCode.ERR_INTERNET_DISCONNECTED,
            CefLoadHandler.ErrorCode.ERR_CONNECTION_TIMED_OUT,
            CefLoadHandler.ErrorCode.ERR_FAILED
        )

        errorCodes.forEach { errorCode ->
            val errorPage = GBrowserErrorPage.create(errorCode, "Test error", "https://example.com")
            assertNotNull(errorPage, "Error page should be generated for error code: $errorCode")
            assertTrue(errorPage.isNotEmpty(), "Error page should not be empty for error code: $errorCode")
        }
    }

    @Test
    fun `test error page contains style information`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_FAILED
        val errorText = "Test error"
        val failedUrl = "https://example.com"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        // Error page should have styling
        assertTrue(errorPage.contains("<style") || errorPage.contains("style="),
                   "Error page should contain style information")
    }

    @Test
    fun `test error page handles long error messages`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_FAILED
        val errorText = "This is a very long error message that contains a lot of detailed information " +
                "about what went wrong during the page load process. It includes technical details " +
                "and potentially helpful debugging information for the user or developer."
        val failedUrl = "https://example.com/very/long/path/that/might/cause/issues"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertNotNull(errorPage)
        assertTrue(errorPage.contains(errorText), "Error page should contain full long error message")
        assertTrue(errorPage.contains(failedUrl), "Error page should contain full long URL")
    }

    @Test
    fun `test error page with Unicode characters`() {
        val errorCode = CefLoadHandler.ErrorCode.ERR_FAILED
        val errorText = "错误信息 - エラー - خطأ - Erreur"
        val failedUrl = "https://例え.jp/パス"

        val errorPage = GBrowserErrorPage.create(errorCode, errorText, failedUrl)

        assertNotNull(errorPage)
        assertTrue(errorPage.isNotEmpty(), "Error page should handle Unicode characters")
    }

    @Test
    fun `test error page with network error codes`() {
        val networkErrors = listOf(
            CefLoadHandler.ErrorCode.ERR_NAME_NOT_RESOLVED,
            CefLoadHandler.ErrorCode.ERR_INTERNET_DISCONNECTED,
            CefLoadHandler.ErrorCode.ERR_CONNECTION_REFUSED,
            CefLoadHandler.ErrorCode.ERR_CONNECTION_RESET
        )

        networkErrors.forEach { errorCode ->
            val errorPage = GBrowserErrorPage.create(
                errorCode,
                "Network error: ${errorCode.name}",
                "https://example.com"
            )
            assertTrue(errorPage.isNotEmpty(), "Should generate error page for ${errorCode.name}")
        }
    }
}
