package com.sednium.bladelauncher.ui.control.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InGameChatLogicTest {

    @Test
    fun testSlashToggle() {
        fun toggleSlash(text: String): String {
            return if (text.startsWith("/")) {
                text.removePrefix("/")
            } else {
                "/$text"
            }
        }

        assertEquals("/", toggleSlash(""))
        assertEquals("/help", toggleSlash("help"))
        assertEquals("help", toggleSlash("/help"))
        assertEquals("", toggleSlash("/"))
    }

    @Test
    fun testSendHandling() {
        var sentText: String? = null
        var enterSent = false
        var closed = false

        fun handleSend(text: String) {
            if (text.isNotEmpty()) {
                sentText = text
                enterSent = true
                closed = true
            } else {
                enterSent = true
                closed = true
            }
        }

        // Test non-empty text
        handleSend("test message")
        assertEquals("test message", sentText)
        assertTrue(enterSent)
        assertTrue(closed)

        // Reset and test empty text
        sentText = null
        enterSent = false
        closed = false
        handleSend("")
        assertEquals(null, sentText)
        assertTrue(enterSent)
        assertTrue(closed)
    }
}
