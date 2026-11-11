package com.github.gbrowser.ui.gcef.impl

import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserToolWindowActionBarDelegate
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * JCEF invokes display-handler callbacks on a CEF thread. The delegate touches Swing
 * (validate/repaint, content manager) and must therefore only run on the EDT — calling it
 * inline from the CEF thread deadlocks against the EDT (issue #417).
 */
class GBrowserCefDisplayChangeHandlerTest {

  private val delegate = mockk<GBrowserToolWindowActionBarDelegate>(relaxed = true)
  private val queued = mutableListOf<Runnable>()
  private val handler = GBrowserCefDisplayChangeHandler(delegate) { queued.add(it) }

  @Test
  fun `address change is not delivered on the calling CEF thread`() {
    handler.onAddressChange(null, null, "https://gitlab.com/group/project/-/merge_requests/1")

    verify(exactly = 0) { delegate.onAddressChange(any()) }
    assertEquals(1, queued.size)

    queued.forEach(Runnable::run)
    verify(exactly = 1) { delegate.onAddressChange("https://gitlab.com/group/project/-/merge_requests/1") }
  }

  @Test
  fun `title change is not delivered on the calling CEF thread`() {
    handler.onTitleChange(null, "Merge request")

    verify(exactly = 0) { delegate.onTitleChange(any()) }
    assertEquals(1, queued.size)

    queued.forEach(Runnable::run)
    verify(exactly = 1) { delegate.onTitleChange("Merge request") }
  }

  @Test
  fun `ignored urls and titles are not dispatched`() {
    handler.onAddressChange(null, null, "devtools://devtools/inspector.html")
    handler.onAddressChange(null, null, "file:///jbcefbrowser/error")
    handler.onAddressChange(null, null, "")
    handler.onTitleChange(null, "DevTools - example.com")
    handler.onTitleChange(null, " ")

    assertEquals(0, queued.size)
  }
}
