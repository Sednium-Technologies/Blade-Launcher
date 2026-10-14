package com.sednium.bladelauncher.game.control

import com.movtery.layer_controller.layout.loadLayoutFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class ControlLayoutTest {
    @Test
    fun testPojavConvertedLayoutLoadsSuccessfully() {
        val file = File("/home/ankush/pojav_controls.json")
        val json = file.readText()
        val layout = loadLayoutFromString(json)
        assertNotNull(layout)
        assertEquals("Pojav Controls", layout.info.name.default)
        assertEquals(1, layout.layers.size)
        assertEquals(21, layout.layers[0].normalButtons.size)
    }
}
