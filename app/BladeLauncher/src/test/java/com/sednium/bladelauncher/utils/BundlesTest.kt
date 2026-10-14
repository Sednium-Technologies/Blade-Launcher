package com.sednium.bladelauncher.utils

import com.sednium.bladelauncher.ui.activities.JvmCrash
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BundlesTest {

    @Test
    fun testJvmCrashPropertiesAndEquality() {
        val crash1 = JvmCrash(code = -3, isSignal = false, logPath = "/path/to/log.txt")
        val crash2 = JvmCrash(code = -3, isSignal = false, logPath = "/path/to/log.txt")
        val crash3 = JvmCrash(code = 1, isSignal = true, logPath = "/other/log.txt")

        assertEquals(-3, crash1.code)
        assertEquals(false, crash1.isSignal)
        assertEquals("/path/to/log.txt", crash1.logPath)

        assertEquals(crash1, crash2)
        assertEquals(crash1.hashCode(), crash2.hashCode())
        assertNotEquals(crash1, crash3)

        val copied = crash1.copy(code = 0)
        assertEquals(0, copied.code)
        assertEquals(crash1.logPath, copied.logPath)
    }
}
