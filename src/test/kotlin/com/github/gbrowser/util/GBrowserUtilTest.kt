package com.github.gbrowser.util

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.SelectionModel
import io.mockk.every
import io.mockk.mockk
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
            "LOCALHOST",
            "LocalHost:3000"
        )

        for (url in localhostUrls) {
            Assertions.assertTrue(GBrowserUtil.isValidBrowserURL(url), "Localhost URL should be valid: $url")
        }
    }
    
    @Test
    fun `test JCEF debug port get and set`() {
        // Create a backup of the current value
        val originalValue = GBrowserUtil.getJCEFDebugPort()
        
        try {
            // Test setting a new value
            GBrowserUtil.setJCEFDebugPort(12345)
            
            // Verify the value was set correctly
            Assertions.assertEquals(12345, GBrowserUtil.getJCEFDebugPort())
        } finally {
            // Restore the original value
            GBrowserUtil.setJCEFDebugPort(originalValue)
        }
    }
    
    @Test
    fun `test getSelectedText with null editor`() {
        val mockEvent = mockk<AnActionEvent>()
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns null
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertNull(result)
    }
    
    @Test
    fun `test getSelectedText with non-main editor`() {
        val mockEvent = mockk<AnActionEvent>()
        val mockEditor = mockk<Editor>()
        
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns mockEditor
        every { mockEditor.editorKind } returns EditorKind.PREVIEW
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertNull(result)
    }
    
    @Test
    fun `test getSelectedText with no selection`() {
        val mockEvent = mockk<AnActionEvent>()
        val mockEditor = mockk<Editor>()
        val mockSelectionModel = mockk<SelectionModel>()
        
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns mockEditor
        every { mockEditor.editorKind } returns EditorKind.MAIN_EDITOR
        every { mockEditor.selectionModel } returns mockSelectionModel
        every { mockSelectionModel.selectedText } returns null
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertNull(result)
    }
    
    @Test
    fun `test getSelectedText with blank selection`() {
        val mockEvent = mockk<AnActionEvent>()
        val mockEditor = mockk<Editor>()
        val mockSelectionModel = mockk<SelectionModel>()
        
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns mockEditor
        every { mockEditor.editorKind } returns EditorKind.MAIN_EDITOR
        every { mockEditor.selectionModel } returns mockSelectionModel
        every { mockSelectionModel.selectedText } returns "   "
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertNull(result)
    }
    
    @Test
    fun `test getSelectedText with invalid URL selection`() {
        val mockEvent = mockk<AnActionEvent>()
        val mockEditor = mockk<Editor>()
        val mockSelectionModel = mockk<SelectionModel>()
        
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns mockEditor
        every { mockEditor.editorKind } returns EditorKind.MAIN_EDITOR
        every { mockEditor.selectionModel } returns mockSelectionModel
        every { mockSelectionModel.selectedText } returns "not a valid url"
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertNull(result)
    }
    
    @Test
    fun `test getSelectedText with valid URL selection`() {
        val mockEvent = mockk<AnActionEvent>()
        val mockEditor = mockk<Editor>()
        val mockSelectionModel = mockk<SelectionModel>()
        
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns mockEditor
        every { mockEditor.editorKind } returns EditorKind.MAIN_EDITOR
        every { mockEditor.selectionModel } returns mockSelectionModel
        every { mockSelectionModel.selectedText } returns "https://example.com"
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertEquals("https://example.com", result)
    }
    
    @Test
    fun `test getSelectedText with URL that needs trimming`() {
        val mockEvent = mockk<AnActionEvent>()
        val mockEditor = mockk<Editor>()
        val mockSelectionModel = mockk<SelectionModel>()
        
        every { mockEvent.getData(CommonDataKeys.EDITOR) } returns mockEditor
        every { mockEditor.editorKind } returns EditorKind.MAIN_EDITOR
        every { mockEditor.selectionModel } returns mockSelectionModel
        every { mockSelectionModel.selectedText } returns "  https://example.com  "
        
        val result = GBrowserUtil.getSelectedText(mockEvent)
        
        Assertions.assertEquals("https://example.com", result)
    }
}
