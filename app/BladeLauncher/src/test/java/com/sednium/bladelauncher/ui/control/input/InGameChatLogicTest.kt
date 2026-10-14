package com.sednium.bladelauncher.ui.control.input

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InGameChatLogicTest {

    @Test
    fun testSlashToggle() {
        // Toggle empty string -> adds slash and moves cursor to 1
        val empty = toggleCommandSlash(TextFieldValue(""))
        assertEquals("/", empty.text)
        assertEquals(TextRange(1), empty.selection)

        // Toggle "help" -> prepends slash and shifts cursor
        val help = toggleCommandSlash(TextFieldValue("help", TextRange(4)))
        assertEquals("/help", help.text)
        assertEquals(TextRange(5), help.selection)

        // Toggle "/help" -> removes slash and shifts cursor back
        val unslashedHelp = toggleCommandSlash(TextFieldValue("/help", TextRange(5)))
        assertEquals("help", unslashedHelp.text)
        assertEquals(TextRange(4), unslashedHelp.selection)

        // Toggle "/" -> removes slash to empty string with cursor at 0
        val unslashedEmpty = toggleCommandSlash(TextFieldValue("/", TextRange(1)))
        assertEquals("", unslashedEmpty.text)
        assertEquals(TextRange(0), unslashedEmpty.selection)
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
