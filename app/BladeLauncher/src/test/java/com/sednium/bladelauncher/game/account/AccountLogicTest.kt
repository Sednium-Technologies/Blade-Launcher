package com.sednium.bladelauncher.game.account

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class AccountLogicTest {

    @Test
    fun testAccountUUIDParsing() {
        val originalUUID = UUID.randomUUID()
        val withDashes = originalUUID.toString()
        val withoutDashes = withDashes.replace("-", "")

        val parsedFromDashes = accountUUID(withDashes)
        assertEquals(originalUUID, parsedFromDashes)

        val parsedFromNoDashes = accountUUID(withoutDashes)
        assertEquals(originalUUID, parsedFromNoDashes)

        val stringFromUUID = accountUUID(originalUUID)
        assertEquals(withoutDashes, stringFromUUID)
    }

    @Test
    fun testPendingUUIDFromUserName() {
        val userName = "MasterBoyAnkush"
        val pendingUUID = getUUIDFromUserName(userName).toString()
        assertTrue(pendingUUID.contains("-"))

        val uuid = accountUUID(pendingUUID)
        val uuidString = accountUUID(uuid)
        assertEquals(32, uuidString.length)
        assertFalse(uuidString.contains("-"))
    }
}
