/*
 * Blade Launcher
 * Copyright (C) 2026 Sednium Technologies and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.sednium.bladelauncher.upgrade

import com.sednium.bladelauncher.ui.upgrade.OtaDownloadState
import com.sednium.bladelauncher.ui.upgrade.OtaUpdater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OtaUpdaterTest {

    @Test
    fun testResolveTargetApk() {
        val remoteFiles = listOf(
            RemoteData.RemoteFile(
                fileName = "BladeLauncher-2.5.0-arm64-v8a.apk",
                uri = "https://example.com/arm64.apk",
                arch = RemoteData.RemoteFile.Arch.ARM64,
                size = 186_000_000L
            ),
            RemoteData.RemoteFile(
                fileName = "BladeLauncher-2.5.0-armeabi-v7a.apk",
                uri = "https://example.com/arm.apk",
                arch = RemoteData.RemoteFile.Arch.ARM,
                size = 181_000_000L
            ),
            RemoteData.RemoteFile(
                fileName = "BladeLauncher-2.5.0-x86_64.apk",
                uri = "https://example.com/x86_64.apk",
                arch = RemoteData.RemoteFile.Arch.X86_64,
                size = 186_000_000L
            )
        )

        val remoteData = RemoteData(
            code = 200032,
            version = "2.5.0",
            createdAt = "2026-11-24T10:00:00Z",
            files = remoteFiles,
            defaultBody = RemoteData.RemoteBody("en", "v2.5.0 changelog"),
            bodies = emptyList()
        )

        val resolved = OtaUpdater.resolveTargetApk(remoteData)
        assertNotNull("Target APK should resolve for current architecture", resolved)
        assertTrue("Resolved APK should belong to data files", remoteFiles.contains(resolved))
    }

    @Test
    fun testOtaDownloadStateProgressCalculation() {
        val state1 = OtaDownloadState.Downloading(
            bytesDownloaded = 50_000_000L,
            totalBytes = 100_000_000L,
            speedBytesPerSec = 5_000_000L
        )
        assertEquals(0.5f, state1.progress, 0.001f)

        val state2 = OtaDownloadState.Downloading(
            bytesDownloaded = 0L,
            totalBytes = -1L,
            speedBytesPerSec = 0L
        )
        assertEquals(-1f, state2.progress, 0.001f)
    }

    @Test
    fun testGetArchLabel() {
        assertEquals("arm64-v8a", OtaUpdater.getArchLabel(RemoteData.RemoteFile.Arch.ARM64))
        assertEquals("armeabi-v7a", OtaUpdater.getArchLabel(RemoteData.RemoteFile.Arch.ARM))
        assertEquals("x86_64", OtaUpdater.getArchLabel(RemoteData.RemoteFile.Arch.X86_64))
        assertEquals("x86", OtaUpdater.getArchLabel(RemoteData.RemoteFile.Arch.X86))
        assertEquals("Universal", OtaUpdater.getArchLabel(RemoteData.RemoteFile.Arch.ALL))
    }

    @Test
    fun testIsArchRecommendedDoesNotThrow() {
        // Must evaluate safely without crashing on any architecture
        val isArm64Rec = OtaUpdater.isArchRecommended(RemoteData.RemoteFile.Arch.ARM64)
        val isArmRec = OtaUpdater.isArchRecommended(RemoteData.RemoteFile.Arch.ARM)
        val isX8664Rec = OtaUpdater.isArchRecommended(RemoteData.RemoteFile.Arch.X86_64)
        assertTrue(isArm64Rec || isArmRec || isX8664Rec)
    }
}
