package com.github.gbrowser.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserUtilTest {

    @Test
    fun `test isValidBrowserURL with valid URLs`() {
        val validUrls = listOf(
            "https://www.example.com",
            "http://example.com",
            "https://subdomain.example.com/path?query=value",
            "http://localhost:8080",
            "https://example.com/path/to/resource.html",
            "ftp://ftp.example.com",
            "file:///path/to/file.txt",
            "www.example.com"
        )

        for (url in validUrls) {
            Assertions.assertTrue(GBrowserUtil.isValidBrowserURL(url), "URL should be valid: $url")
        }
    }

    @Test
    fun `test isValidBrowserURL with invalid URLs`() {
        val invalidUrls = listOf(
            "",
            " ",
            "not a url",
            "https://", // Missing domain
            "example" // No TLD
        )

        for (url in invalidUrls) {
            Assertions.assertFalse(GBrowserUtil.isValidBrowserURL(url), "URL should be invalid: $url")
        }
    }

    @Test
    fun `test isValidBrowserURL with malformed but accepted URLs`() {
        // These URLs are technically malformed but are accepted by the current implementation
        val malformedButAcceptedUrls = listOf(
            "http:/example.com", // Missing slash
            "http:example.com" // Missing slashes
        )

        for (url in malformedButAcceptedUrls) {
            Assertions.assertTrue(GBrowserUtil.isValidBrowserURL(url), "URL should be accepted: $url")
        }
    }

    @Test
    fun `test isValidBrowserURL with localhost variations`() {
        val localhostUrls = listOf(
            "localhost",
            "localhost:8080",
            "http://localhost",
            "https://localhost:8443",
            "LOCALHOST", // Case insensitive
            "LocalHost:3000"
        )

        for (url in localhostUrls) {
            Assertions.assertTrue(GBrowserUtil.isValidBrowserURL(url), "Localhost URL should be valid: $url")
        }
    }
}
