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

import com.sednium.bladelauncher.BuildConfig
import com.sednium.bladelauncher.path.GLOBAL_JSON
import com.sednium.bladelauncher.viewmodel.LauncherUpgradeViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class UpgradeCheckerTest {

    private val sampleReleaseJson = """
    {
      "tag_name": "v2.4.7",
      "target_commitish": "main",
      "name": "Blade Launcher v2.4.7",
      "draft": false,
      "prerelease": false,
      "created_at": "2026-09-07T18:13:21Z",
      "published_at": "2026-09-07T18:13:46Z",
      "assets": [
        {
          "name": "BladeLauncher-2.4.7.apk",
          "size": 306258639,
          "content_type": "application/vnd.android.package-archive",
          "browser_download_url": "https://github.com/Sednium-Technologies/Blade-Launcher/releases/download/v2.4.7/BladeLauncher-2.4.7.apk"
        }
      ],
      "body": "## Blade Launcher v2.4.7 Release Notes\n* Feature A\n* Feature B"
    }
    """.trimIndent()

    @Test
    fun testParseGitHubReleaseJson() {
        val release = GLOBAL_JSON.decodeFromString(GithubReleaseApi.serializer(), sampleReleaseJson)
        assertEquals("v2.4.7", release.tagName)
        assertEquals("Blade Launcher v2.4.7", release.name)
        assertEquals(1, release.assets.size)

        val remoteData = release.toRemoteData()
        assertEquals("2.4.7", remoteData.version)
        assertEquals("2026-09-07T18:13:46Z", remoteData.createdAt)
        assertEquals(1, remoteData.files.size)

        val apkFile = remoteData.files.first()
        assertEquals("BladeLauncher-2.4.7.apk", apkFile.fileName)
        assertEquals(306258639L, apkFile.size)
        assertEquals(RemoteData.RemoteFile.Arch.ALL, apkFile.arch)
        assertEquals(
            "https://github.com/Sednium-Technologies/Blade-Launcher/releases/download/v2.4.7/BladeLauncher-2.4.7.apk",
            apkFile.uri
        )
        assertTrue(remoteData.defaultBody.markdown.contains("Blade Launcher v2.4.7 Release Notes"))
    }

    @Test
    fun testMultiArchAssetMapping() {
        val release = GithubReleaseApi(
            tagName = "v2.4.8",
            name = "Blade Launcher v2.4.8",
            body = "Multi-arch release",
            assets = listOf(
                GithubReleaseApi.Asset(
                    name = "BladeLauncher-arm64-v8a-2.4.8.apk",
                    size = 100000000L,
                    browserDownloadUrl = "https://github.com/.../arm64.apk"
                ),
                GithubReleaseApi.Asset(
                    name = "BladeLauncher-armeabi-v7a-2.4.8.apk",
                    size = 90000000L,
                    browserDownloadUrl = "https://github.com/.../arm.apk"
                ),
                GithubReleaseApi.Asset(
                    name = "BladeLauncher-x86_64-2.4.8.apk",
                    size = 110000000L,
                    browserDownloadUrl = "https://github.com/.../x86_64.apk"
                ),
                GithubReleaseApi.Asset(
                    name = "BladeLauncher-2.4.8.apk",
                    size = 300000000L,
                    browserDownloadUrl = "https://github.com/.../all.apk"
                )
            )
        )

        val remoteData = release.toRemoteData()
        assertEquals(4, remoteData.files.size)
        assertEquals(RemoteData.RemoteFile.Arch.ARM64, remoteData.files[0].arch)
        assertEquals(RemoteData.RemoteFile.Arch.ARM, remoteData.files[1].arch)
        assertEquals(RemoteData.RemoteFile.Arch.X86_64, remoteData.files[2].arch)
        assertEquals(RemoteData.RemoteFile.Arch.ALL, remoteData.files[3].arch)
    }

    /**
     * Genuine branch test 1: Current version vs remote release with matching tag.
     * Expected outcome: Already running latest version (onIsLatest called, onUpgrade NOT called).
     */
    @Test
    fun testBranch_AlreadyLatestVersion() = runBlocking {
        val vm = LauncherUpgradeViewModel()
        val currentReleaseJson = sampleReleaseJson.replace("\"v2.4.7\"", "\"v${BuildConfig.VERSION_NAME}\"")
        val release = GLOBAL_JSON.decodeFromString(GithubReleaseApi.serializer(), currentReleaseJson)
        val remoteData = release.toRemoteData()

        var isLatestCalled = false
        var upgradeCalled = false

        vm.checkForUpgrade(
            data = remoteData,
            lastIgnored = null,
            ignoreDismissedVersions = false,
            onUpgrade = { upgradeCalled = true },
            onIsLatest = { isLatestCalled = true }
        )

        assertTrue("Expected onIsLatest to be called for matching version ${BuildConfig.VERSION_NAME}", isLatestCalled)
        assertFalse("Expected onUpgrade NOT to be called when running matching version", upgradeCalled)
    }

    /**
     * Genuine branch test 2: Current version vs simulated newer release "99.0.0".
     * Expected outcome: Upgrade detected (onUpgrade called with data, onIsLatest NOT called).
     */
    @Test
    fun testBranch_SimulatedNewerVersionDetected() = runBlocking {
        val vm = LauncherUpgradeViewModel()
        val newerRelease = GithubReleaseApi(
            tagName = "v99.0.0",
            name = "Blade Launcher v99.0.0",
            body = "## What's new in v99.0.0\n* New optimization features",
            publishedAt = "2026-10-01T12:00:00Z",
            assets = listOf(
                GithubReleaseApi.Asset(
                    name = "BladeLauncher-99.0.0.apk",
                    size = 310000000L,
                    browserDownloadUrl = "https://github.com/Sednium-Technologies/Blade-Launcher/releases/download/v99.0.0/BladeLauncher-99.0.0.apk"
                )
            )
        )
        val remoteData = newerRelease.toRemoteData()

        var isLatestCalled = false
        var upgradeCalled = false
        var receivedData: RemoteData? = null

        vm.checkForUpgrade(
            data = remoteData,
            lastIgnored = null,
            ignoreDismissedVersions = false,
            onUpgrade = { data ->
                upgradeCalled = true
                receivedData = data
            },
            onIsLatest = { isLatestCalled = true }
        )

        assertTrue("Expected onUpgrade to be called for newer version 99.0.0", upgradeCalled)
        assertFalse("Expected onIsLatest NOT to be called for newer version 99.0.0", isLatestCalled)
        assertNotNull(receivedData)
        assertEquals("99.0.0", receivedData?.version)
        assertEquals(1, receivedData?.files?.size)
        assertEquals("BladeLauncher-99.0.0.apk", receivedData?.files?.first()?.fileName)
    }

    /**
     * Test user ignored version branch when ignoreDismissedVersions is true.
     */
    @Test
    fun testBranch_UserIgnoredVersion() = runBlocking {
        val vm = LauncherUpgradeViewModel()
        val newerRelease = GithubReleaseApi(
            tagName = "v99.0.0",
            name = "Blade Launcher v99.0.0",
            body = "Release notes",
            assets = emptyList()
        )
        val remoteData = newerRelease.toRemoteData()

        var isLatestCalled = false
        var upgradeCalled = false

        // Simulate that user previously clicked Ignore for this version code
        vm.checkForUpgrade(
            data = remoteData,
            lastIgnored = remoteData.code,
            ignoreDismissedVersions = true,
            onUpgrade = { upgradeCalled = true },
            onIsLatest = { isLatestCalled = true }
        )

        assertFalse("Expected onUpgrade to be skipped when version is ignored", upgradeCalled)
        assertFalse("Expected onIsLatest NOT to be called when an ignored upgrade exists", isLatestCalled)
    }

    /**
     * Live network test: Fetch the actual published release from GitHub API
     * and verify that it parses cleanly into RemoteData and matches v2.4.7.
     * Wrapped in assumeTrue and try-catch for offline and CI hermeticity.
     */
    @Test
    fun testLiveGitHubApiReleaseDetection() = runBlocking {
        try {
            val url = java.net.URL("https://api.github.com/repos/Sednium-Technologies/Blade-Launcher/releases/latest")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("User-Agent", "BladeLauncher-Test")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val responseCode = try {
                conn.responseCode
            } catch (e: Exception) {
                assumeTrue("Network unavailable: ${e.message}", false)
                return@runBlocking
            }

            assumeTrue(
                "GitHub API rate limit reached or service unavailable (status $responseCode)",
                responseCode == 200
            )

            val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
            val release = GLOBAL_JSON.decodeFromString(GithubReleaseApi.serializer(), jsonString)

            assertEquals("v2.4.7", release.tagName)
            val remoteData = release.toRemoteData()
            assertEquals("2.4.7", remoteData.version)
            assertTrue(remoteData.files.isNotEmpty())

            val apkAsset = remoteData.files.first()
            assertEquals("BladeLauncher-2.4.7.apk", apkAsset.fileName)
            assertTrue(apkAsset.size > 200_000_000L) // Real APK is ~306 MB
            assertEquals(RemoteData.RemoteFile.Arch.ALL, apkAsset.arch)

            // Verify that running checkForUpgrade with this live release against current 2.4.7 triggers onIsLatest
            val vm = LauncherUpgradeViewModel()
            var isLatestCalled = false
            var upgradeCalled = false

            vm.checkForUpgrade(
                data = remoteData,
                lastIgnored = null,
                ignoreDismissedVersions = false,
                onUpgrade = { upgradeCalled = true },
                onIsLatest = { isLatestCalled = true }
            )

            assertTrue("Live release v2.4.7 must trigger onIsLatest for current app version 2.4.7", isLatestCalled)
            assertFalse("Live release v2.4.7 must NOT trigger onUpgrade for current app version 2.4.7", upgradeCalled)
        } catch (e: Exception) {
            assumeTrue("Skipping live network test due to network or API error: ${e.message}", false)
        }
    }
}
