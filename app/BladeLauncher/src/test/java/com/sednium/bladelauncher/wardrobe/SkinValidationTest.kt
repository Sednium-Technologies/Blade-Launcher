package com.sednium.bladelauncher.wardrobe

import android.graphics.BitmapFactory
import com.sednium.bladelauncher.game.account.wardrobe.isClassicSkin
import com.sednium.bladelauncher.game.account.wardrobe.isDualLayerSkin
import com.sednium.bladelauncher.game.account.wardrobe.isValidCape
import com.sednium.bladelauncher.game.account.wardrobe.isValidSkin
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkinValidationTest {

    private fun createOptions(width: Int, height: Int): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.outWidth = width
        options.outHeight = height
        return options
    }

    @Test
    fun testStandardSkins() {
        // Standard 64x64 dual layer skin
        assertTrue(createOptions(64, 64).isValidSkin())
        assertTrue(createOptions(64, 64).isDualLayerSkin())

        // Classic 64x32 single layer skin
        assertTrue(createOptions(64, 32).isValidSkin())
        assertTrue(createOptions(64, 32).isClassicSkin())
    }

    @Test
    fun testHDScaledSkins() {
        // HD 128x128 skin
        assertTrue(createOptions(128, 128).isValidSkin())
        assertTrue(createOptions(128, 128).isDualLayerSkin())

        // HD 256x256 skin
        assertTrue(createOptions(256, 256).isValidSkin())
        assertTrue(createOptions(256, 256).isDualLayerSkin())

        // HD 512x512 skin
        assertTrue(createOptions(512, 512).isValidSkin())
        assertTrue(createOptions(512, 512).isDualLayerSkin())

        // HD 1024x1024 skin
        assertTrue(createOptions(1024, 1024).isValidSkin())
        assertTrue(createOptions(1024, 1024).isDualLayerSkin())

        // HD 128x64 classic skin
        assertTrue(createOptions(128, 64).isValidSkin())
        assertTrue(createOptions(128, 64).isClassicSkin())

        // HD 256x128 classic skin
        assertTrue(createOptions(256, 128).isValidSkin())
        assertTrue(createOptions(256, 128).isClassicSkin())
    }

    @Test
    fun testInvalidSkins() {
        // Non-multiple or non-skin dimensions
        assertFalse(createOptions(0, 0).isValidSkin())
        assertFalse(createOptions(-1, -1).isValidSkin())
        assertFalse(createOptions(100, 100).isValidSkin())
        assertFalse(createOptions(1920, 1080).isValidSkin())
        assertFalse(createOptions(64, 48).isValidSkin())
        assertFalse(createOptions(32, 32).isValidSkin())
    }

    @Test
    fun testCapeValidation() {
        // Standard 64x32 cape
        assertTrue(createOptions(64, 32).isValidCape())

        // HD 128x64 cape
        assertTrue(createOptions(128, 64).isValidCape())

        // HD 256x128 cape
        assertTrue(createOptions(256, 128).isValidCape())

        // Square 64x64 cape
        assertTrue(createOptions(64, 64).isValidCape())

        // Square HD 128x128 cape
        assertTrue(createOptions(128, 128).isValidCape())

        // Invalid cape
        assertFalse(createOptions(100, 50).isValidCape())
        assertFalse(createOptions(0, 0).isValidCape())
        assertFalse(createOptions(800, 600).isValidCape())
    }
}
