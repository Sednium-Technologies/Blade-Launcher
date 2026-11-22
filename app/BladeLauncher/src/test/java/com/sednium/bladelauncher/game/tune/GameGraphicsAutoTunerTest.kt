package com.sednium.bladelauncher.game.tune

import com.sednium.bladelauncher.utils.device.DeviceHardwareProfiler
import com.sednium.bladelauncher.utils.device.GpuFamily
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GameGraphicsAutoTunerTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    private lateinit var gameDir: File

    @Before
    fun setUp() {
        gameDir = tempFolder.newFolder("minecraft")
    }

    @After
    fun tearDown() {
        DeviceHardwareProfiler.setGpuForTesting(null)
    }

    @Test
    fun testAutoTuneDegradedOptionsTxt() {
        val optionsFile = File(gameDir, "options.txt")
        optionsFile.writeText(
            """
            ao:false
            cutoutLeaves:false
            mipmapLevels:0
            graphicsPreset:"custom"
            renderClouds:"false"
            """.trimIndent()
        )

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.QUALCOMM_ADRENO)
        GameGraphicsAutoTuner.tuneSettings(gameDir)

        val content = optionsFile.readText()
        assertTrue("ao should be true", content.contains("ao:true"))
        assertFalse("ao:false should be replaced", content.contains("ao:false"))

        assertTrue("cutoutLeaves should be true", content.contains("cutoutLeaves:true"))
        assertFalse("cutoutLeaves:false should be replaced", content.contains("cutoutLeaves:false"))

        assertTrue("mipmapLevels should be 4", content.contains("mipmapLevels:4"))
        assertFalse("mipmapLevels:0 should be replaced", content.contains("mipmapLevels:0"))

        assertTrue("graphicsPreset should be fancy", content.contains("graphicsPreset:\"fancy\""))
        assertFalse("graphicsPreset:\"custom\" should be replaced", content.contains("graphicsPreset:\"custom\""))

        assertTrue("renderClouds should be true", content.contains("renderClouds:\"true\""))
        assertFalse("renderClouds:\"false\" should be replaced", content.contains("renderClouds:\"false\""))

        assertTrue("renderDistance should be auto-set when missing", content.contains("renderDistance:10"))
    }

    @Test
    fun testGraphicsPresetFastReplacedWithFancy() {
        val optionsFile = File(gameDir, "options.txt")
        optionsFile.writeText("graphicsPreset:\"fast\"\n")

        GameGraphicsAutoTuner.tuneSettings(gameDir)

        val content = optionsFile.readText()
        assertTrue(content.contains("graphicsPreset:\"fancy\""))
        assertFalse(content.contains("graphicsPreset:\"fast\""))
    }

    @Test
    fun testUserRenderDistancePreserved() {
        val optionsFile = File(gameDir, "options.txt")
        optionsFile.writeText(
            """
            renderDistance:16
            ao:false
            """.trimIndent()
        )

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.POWERVR) // optimal would be 4
        GameGraphicsAutoTuner.tuneSettings(gameDir)

        val content = optionsFile.readText()
        assertTrue("User renderDistance of 16 must be preserved", content.contains("renderDistance:16"))
        assertFalse("Optimal chunk distance should not override user choice", content.contains("renderDistance:4"))
        assertTrue("ao should still be tuned", content.contains("ao:true"))
    }

    @Test
    fun testFreshInstallCreatesOptionsTxtWithOptimalChunkDistance() {
        val optionsFile = File(gameDir, "options.txt")
        assertFalse(optionsFile.exists())

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.ARM_MALI) // optimal is 6
        GameGraphicsAutoTuner.tuneSettings(gameDir)

        assertTrue("options.txt should be created on fresh install", optionsFile.exists())
        val content = optionsFile.readText()
        assertTrue("options.txt should contain optimal renderDistance:6", content.contains("renderDistance:6"))
    }

    @Test
    fun testSkyboxifyJsonRenderStarsUpdated() {
        val configDir = File(gameDir, "config").apply { mkdirs() }
        val skyboxifyFile = File(configDir, "skyboxify.json")
        skyboxifyFile.writeText(
            """
            {
              "renderStars": false,
              "renderSun": true,
              "renderMoon": true
            }
            """.trimIndent()
        )

        GameGraphicsAutoTuner.tuneSettings(gameDir)

        val content = skyboxifyFile.readText()
        assertTrue("renderStars should be true", content.contains("\"renderStars\": true"))
        assertFalse("renderStars: false should be gone", content.contains("\"renderStars\": false"))
        assertTrue("renderSun should remain preserved", content.contains("\"renderSun\": true"))
    }

    @Test
    fun testSkyboxifyJsonAlreadyTrueNotModified() {
        val configDir = File(gameDir, "config").apply { mkdirs() }
        val skyboxifyFile = File(configDir, "skyboxify.json")
        skyboxifyFile.writeText(
            """
            {
              "renderStars": true
            }
            """.trimIndent()
        )

        GameGraphicsAutoTuner.tuneSettings(gameDir)

        val content = skyboxifyFile.readText()
        assertTrue(content.contains("\"renderStars\": true"))
    }

    @Test
    fun testNonExistentConfigDirDoesNotCrash() {
        val nonExistentDir = File(gameDir, "does_not_exist")
        // Should not throw any exception
        GameGraphicsAutoTuner.tuneSettings(nonExistentDir)
    }
}
