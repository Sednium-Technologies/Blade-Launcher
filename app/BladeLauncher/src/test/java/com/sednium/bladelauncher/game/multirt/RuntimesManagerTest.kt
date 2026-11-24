/*
 * Blade Launcher
 * Copyright (C) 2026 Sednium Technologies and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sednium.bladelauncher.game.multirt

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import com.sednium.bladelauncher.game.addons.mirror.mapBMCLMirrorUrls
import com.sednium.bladelauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import java.io.File
import java.io.IOException

class RuntimesManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testJdkDownloadManagerCatalog_validUrls() {
        val jdks = JdkDownloadManager.availableJdks
        assertTrue("Available JDKs should contain LTS versions 8, 17, 21, 25", jdks.size >= 4)

        val versions = jdks.map { it.majorVersion }
        assertTrue("Java 8 must be present", versions.contains(8))
        assertTrue("Java 17 must be present", versions.contains(17))
        assertTrue("Java 21 must be present", versions.contains(21))
        assertTrue("Java 25 must be present", versions.contains(25))

        for (item in jdks) {
            assertNotNull("Runtime name must not be null", item.runtimeName)
            assertTrue("Download URLs must contain arm64-v8a", item.downloadUrls.containsKey("arm64-v8a"))
            assertTrue("Download URLs must contain arm64", item.downloadUrls.containsKey("arm64"))
            assertTrue("Download URLs must contain armeabi-v7a", item.downloadUrls.containsKey("armeabi-v7a"))
            assertTrue("Download URLs must contain arm", item.downloadUrls.containsKey("arm"))
            assertTrue("Download URLs must contain x86_64", item.downloadUrls.containsKey("x86_64"))

            for ((arch, url) in item.downloadUrls) {
                assertFalse("URL for $arch must not point to dead pojav-release: $url", url.contains("pojav-release"))
                assertTrue("URL for $arch must be a valid https link: $url", url.startsWith("https://"))
                assertTrue("URL for $arch must point to tar.xz package: $url", url.endsWith(".tar.xz"))
            }
        }
    }

    @Test
    fun testFlattenNestedRuntimeDir() {
        val rootDir = tempFolder.newFolder("Internal-8")
        val nestedDir = File(rootDir, "java-8-openjdk").apply { mkdirs() }
        val binDir = File(nestedDir, "bin").apply { mkdirs() }
        val javaBin = File(binDir, "java").apply { writeText("mock_binary") }
        val releaseFile = File(nestedDir, "release").apply {
            writeText("JAVA_VERSION=\"1.8.0_512\"\nOS_ARCH=\"aarch64\"\n")
        }

        assertTrue(javaBin.exists())
        assertTrue(releaseFile.exists())

        // Flatten the directory
        RuntimesManager.flattenNestedRuntimeDir(rootDir)

        // Nested folder should be gone
        assertFalse(File(rootDir, "java-8-openjdk").exists())

        // Files should now be at the root of rootDir
        val flatRelease = File(rootDir, "release")
        val flatJava = File(rootDir, "bin/java")
        assertTrue("release must exist at root after flattening", flatRelease.exists())
        assertTrue("bin/java must exist at root after flattening", flatJava.exists())
        assertEquals("JAVA_VERSION=\"1.8.0_512\"\nOS_ARCH=\"aarch64\"\n", flatRelease.readText())
    }

    @Test
    fun testFlattenNestedRuntimeDir_alreadyFlatNoOp() {
        val rootDir = tempFolder.newFolder("Internal-17")
        val binDir = File(rootDir, "bin").apply { mkdirs() }
        val javaBin = File(binDir, "java").apply { writeText("mock_java_17") }
        val releaseFile = File(rootDir, "release").apply { writeText("JAVA_VERSION=\"17.0.2\"\n") }

        RuntimesManager.flattenNestedRuntimeDir(rootDir)

        assertTrue(javaBin.exists())
        assertTrue(releaseFile.exists())
    }

    @Test
    fun testIsRuntimeComplete_missingDirReturnsFalse() {
        assertFalse(RuntimesManager.isRuntimeComplete("NonExistentRuntime_XYZ_123"))
    }

    @Test
    fun testGetRuntimeHome_brokenRuntimeThrowsAndCleansUp() {
        val brokenName = "Corrupted-Runtime-Test"
        val brokenDir = File(RuntimesManager.RUNTIME_FOLDER, brokenName).apply { mkdirs() }
        File(brokenDir, "junk.tmp").writeText("corrupt")

        assertTrue("Broken directory must exist initially", brokenDir.exists())

        try {
            RuntimesManager.getRuntimeHome(brokenName)
            fail("getRuntimeHome must throw IOException for incomplete/corrupted runtime")
        } catch (e: IOException) {
            assertTrue("Exception message should indicate runtime is incomplete or corrupted: ${e.message}",
                e.message?.contains("incomplete or corrupted") == true)
        }

        assertFalse("Broken directory must be deleted after failure", brokenDir.exists())
    }

    @Test
    fun testBmclMirrorUrls_alwaysProvidesFallback() {
        val mojongUrl = "https://libraries.minecraft.net/com/mojang/patchy/1.3.9/patchy-1.3.9.jar"
        val urls = mojongUrl.mapBMCLMirrorUrls()
        assertTrue("Mirror list must contain at least 2 URLs (primary and mirror fallback)", urls.size >= 2)
        assertTrue("Primary official URL must be present", urls.contains(mojongUrl))
        assertTrue("BMCL mirror URL must be present", urls.any { it.contains("bmclapi") })
    }

    @Test
    fun testMcimMirrorUrls_alwaysProvidesFallback() {
        val curseforgeUrl = "https://edge.forgecdn.net/files/123/456/test.jar"
        val urls = curseforgeUrl.mapMCIMMirrorUrls()
        assertTrue("MCIM mirror list must contain at least 2 URLs", urls.size >= 2)
        assertTrue("Primary URL must be present", urls.contains(curseforgeUrl))
        assertTrue("MCIM mirror URL must be present", urls.any { it.contains("mcimirror") })
    }
}
