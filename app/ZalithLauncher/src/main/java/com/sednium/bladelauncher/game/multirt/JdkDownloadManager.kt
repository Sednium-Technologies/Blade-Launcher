/*
 * Zalith Launcher 2 / Blade Launcher
 * Copyright (C) 2025-2026 MovTery and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sednium.bladelauncher.game.multirt

import android.content.Context
import com.sednium.bladelauncher.ZLApplication
import com.sednium.bladelauncher.path.DOWNLOAD_OKHTTP_CLIENT
import com.sednium.bladelauncher.path.PathManager
import com.sednium.bladelauncher.utils.device.Architecture
import com.sednium.bladelauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

private const val TAG = "JdkDownloadManager"

data class JdkCatalogItem(
    val majorVersion: Int,
    val runtimeName: String,
    val title: String,
    val description: String,
    val recommendedMc: String,
    val isLts: Boolean,
    val downloadUrls: Map<String, String> // arch -> url
)

object JdkDownloadManager {
    private val _downloadingVersions = MutableStateFlow<Map<Int, Float>>(emptyMap())
    val downloadingVersions = _downloadingVersions.asStateFlow()

    val availableJdks: List<JdkCatalogItem> = listOf(
        JdkCatalogItem(
            majorVersion = 8,
            runtimeName = "Internal-8",
            title = "Java 8 (LTS)",
            description = "Legacy Java runtime for older Minecraft versions and Forge 1.7 - 1.16.5.",
            recommendedMc = "Minecraft ≤ 1.16.5",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre8-pojav-release/jre8-arm64.tar.xz",
                "armeabi-v7a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre8-pojav-release/jre8-arm.tar.xz",
                "x86_64" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre8-pojav-release/jre8-x86_64.tar.xz",
                "x86" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre8-pojav-release/jre8-x86.tar.xz"
            )
        ),
        JdkCatalogItem(
            majorVersion = 11,
            runtimeName = "Internal-11",
            title = "Java 11 (LTS)",
            description = "Intermediate Java 11 runtime for custom modded setups and server software.",
            recommendedMc = "Minecraft 1.16+ / Custom",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre11-pojav-release/jre11-arm64.tar.xz",
                "armeabi-v7a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre11-pojav-release/jre11-arm.tar.xz",
                "x86_64" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre11-pojav-release/jre11-x86_64.tar.xz",
                "x86" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre11-pojav-release/jre11-x86.tar.xz"
            )
        ),
        JdkCatalogItem(
            majorVersion = 17,
            runtimeName = "Internal-17",
            title = "Java 17 (LTS)",
            description = "Standard runtime for modern Minecraft releases from Caves & Cliffs to 1.20.4.",
            recommendedMc = "Minecraft 1.17 – 1.20.4",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-pojav-release/jre17-arm64.tar.xz",
                "armeabi-v7a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-pojav-release/jre17-arm.tar.xz",
                "x86_64" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-pojav-release/jre17-x86_64.tar.xz",
                "x86" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre17-pojav-release/jre17-x86.tar.xz"
            )
        ),
        JdkCatalogItem(
            majorVersion = 21,
            runtimeName = "Internal-21",
            title = "Java 21 (LTS)",
            description = "Latest Long-Term Support Java runtime required by Minecraft 1.20.5+ and 1.21 Tricky Trials.",
            recommendedMc = "Minecraft 1.20.5 – 1.21.x",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre21-pojav-release/jre21-arm64.tar.xz",
                "armeabi-v7a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre21-pojav-release/jre21-arm.tar.xz",
                "x86_64" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre21-pojav-release/jre21-x86_64.tar.xz",
                "x86" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre21-pojav-release/jre21-x86.tar.xz"
            )
        ),
        JdkCatalogItem(
            majorVersion = 25,
            runtimeName = "Internal-25",
            title = "Java 25 (LTS)",
            description = "Next-generation runtime featuring enhanced JIT and ZGC for Minecraft 26.1+.",
            recommendedMc = "Minecraft 26.1+",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre25-pojav-release/jre25-arm64.tar.xz",
                "x86_64" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre25-pojav-release/jre25-x86_64.tar.xz"
            )
        ),
        JdkCatalogItem(
            majorVersion = 26,
            runtimeName = "Internal-26",
            title = "Java 26 (Experimental)",
            description = "Cutting-edge Java 26 build for high performance testing and upcoming snapshot features.",
            recommendedMc = "Snapshots & Experimental",
            isLts = false,
            downloadUrls = mapOf(
                "arm64-v8a" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre26-pojav-release/jre26-arm64.tar.xz",
                "x86_64" to "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre26-pojav-release/jre26-x86_64.tar.xz"
            )
        )
    )

    fun isJdkInstalled(majorVersion: Int): Boolean {
        return RuntimesManager.getRuntimes().any { it.javaVersion == majorVersion }
    }

    suspend fun downloadAndInstallJdk(
        context: Context,
        item: JdkCatalogItem,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val archString = Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE)
        val url = item.downloadUrls[archString]
            ?: item.downloadUrls["arm64-v8a"]
            ?: run {
                onError(IllegalStateException("No compatible download URL found for arch $archString"))
                return@withContext
            }

        val tempFile = File(context.cacheDir, "jdk_${item.majorVersion}_$archString.tar.xz")

        try {
            _downloadingVersions.value = _downloadingVersions.value + (item.majorVersion to 0f)

            val request = Request.Builder()
                .url(url)
                .build()

            DOWNLOAD_OKHTTP_CLIENT.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("Download failed with HTTP ${response.code}")
                val body = response.body ?: throw IllegalStateException("Empty response body")
                val totalLength = body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var downloaded: Long = 0

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            if (totalLength > 0) {
                                val progress = downloaded.toFloat() / totalLength.toFloat()
                                onProgress(progress)
                                _downloadingVersions.value = _downloadingVersions.value + (item.majorVersion to progress)
                            }
                        }
                        output.flush()
                    }
                }
            }

            // Unpack and install into RuntimesManager
            tempFile.inputStream().use { stream ->
                RuntimesManager.installRuntime(
                    nativeLibDir = PathManager.DIR_NATIVE_LIB,
                    inputStream = stream,
                    name = item.runtimeName
                )
            }

            _downloadingVersions.value = _downloadingVersions.value - item.majorVersion
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } catch (e: Throwable) {
            Logger.error(TAG, "Failed to download and install JDK ${item.majorVersion}", e)
            _downloadingVersions.value = _downloadingVersions.value - item.majorVersion
            withContext(Dispatchers.Main) {
                onError(e)
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
