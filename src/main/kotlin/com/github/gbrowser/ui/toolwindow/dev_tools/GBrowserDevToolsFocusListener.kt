package com.github.gbrowser.ui.toolwindow.dev_tools

import com.github.gbrowser.ui.gcef.impl.GBrowserCefKeyBordHandler
import org.cef.CefClient
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.util.*

class GBrowserDevToolsFocusListener(private val client: CefClient) : FocusAdapter() {
    private var lastDate: Date = Date()
    private var waitBeforeNext: Boolean = false

    private fun preventFocusFlicker() {
        val date = Date()
        val duration = date.time - lastDate.time

        // Increase the threshold for focus change detection
        // This prevents excessive focus changes in short time periods
        if (!waitBeforeNext && duration in 1..200) {
            waitBeforeNext = true

            // Don't disable focusability - this can cause UI hangs
            // Instead, just set a timer to prevent rapid focus changes
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    waitBeforeNext = false
                }
            }, 250) // Longer delay to prevent focus flicker
        }
        lastDate = date
    }

    override fun focusGained(e: FocusEvent?) {
        client.removeKeyboardHandler()
        client.addKeyboardHandler(GBrowserCefKeyBordHandler())
    }

    override fun focusLost(e: FocusEvent?) {
        preventFocusFlicker()
        client.removeKeyboardHandler()
    }
}
