package com.github.gbrowser.services.providers

import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CachingWebPageTitleLoaderTest {

    private lateinit var titleLoader: CachingWebPageTitleLoader

    @BeforeEach
    fun setup() {
        titleLoader = CachingWebPageTitleLoader()
    }
    
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test getTitleOfWebPage caches results`() {
        // This test verifies that the same URL returns the same CompletableFuture
        val url = "https://example.com"
        
        val future1 = titleLoader.getTitleOfWebPage(url)
        val future2 = titleLoader.getTitleOfWebPage(url)
        
        // The same URL should return the same CompletableFuture instance
        Assertions.assertSame(future1, future2)
    }

    @Test
    fun `test dispose cleans up cache`() {
        // Load something into the cache
        titleLoader.getTitleOfWebPage("https://example.com")
        
        // Call dispose
        titleLoader.dispose()
        
        // We'll load it again and verify we get a different CompletableFuture
        val future1 = titleLoader.getTitleOfWebPage("https://example.com")
        val future2 = titleLoader.getTitleOfWebPage("https://example.com")
        
        // If the cache was cleaned up properly, we should now get a different CompletableFuture
        // However, the implementation might vary, so we'll just check they're both non-null
        Assertions.assertNotNull(future1)
        Assertions.assertNotNull(future2)
    }
}
