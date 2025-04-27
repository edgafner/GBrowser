package com.github.gbrowser.settings.dao

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GBrowserHistoryTest {

    @Test
    fun `test equals with same URL but different names`() {
        val history1 = GBrowserHistory("Page 1", "https://example.com")
        val history2 = GBrowserHistory("Page 2", "https://example.com")

        Assertions.assertEquals(history1, history2, "Histories with same URL should be equal")
        Assertions.assertEquals(history1.hashCode(), history2.hashCode(), "Hash codes should be equal for equal objects")
    }

    @Test
    fun `test equals with different URLs`() {
        val history1 = GBrowserHistory("Page 1", "https://example1.com")
        val history2 = GBrowserHistory("Page 1", "https://example2.com")

        Assertions.assertNotEquals(history1, history2, "Histories with different URLs should not be equal")
        Assertions.assertNotEquals(history1.hashCode(), history2.hashCode(), "Hash codes should not be equal for unequal objects")
    }

    @Test
    fun `test equals with null and different type`() {
        val history = GBrowserHistory("Page 1", "https://example.com")

        Assertions.assertNotEquals(history, null, "History should not be equal to null")
        Assertions.assertNotEquals(history, "not a history object", "History should not be equal to different type")
    }

    @Test
    fun `test equals with same object reference`() {
        val history = GBrowserHistory("Page 1", "https://example.com")

        Assertions.assertEquals(history, history, "History should be equal to itself")
    }

    @Test
    fun `test serialVersionUID constant`() {
        // Just verify that the constant exists and has the expected value
        Assertions.assertEquals(12143532789876L, GBrowserHistory.serialVersionUID)
    }
}
