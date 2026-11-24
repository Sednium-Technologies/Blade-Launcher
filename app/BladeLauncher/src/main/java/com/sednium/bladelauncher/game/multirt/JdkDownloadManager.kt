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
import android.widget.Toast
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
import java.io.IOException

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

    // HostCraft Android OpenJDK build mirror (pinned active releases with pre-built aarch64, arm, x86_64, x86)
    private const val HOSTRUNTIME_BASE_URL = "https://github.com/Project-crafting/openjdk-android-hostcraft/releases/download/openjdk-2026.11.01-0456"

    val availableJdks: List<JdkCatalogItem> = listOf(
        JdkCatalogItem(
            majorVersion = 8,
            runtimeName = "Internal-8",
            title = "Java 8 (LTS)",
            description = "Legacy Java runtime for older Minecraft versions and Forge 1.7 - 1.16.5.",
            recommendedMc = "Minecraft ≤ 1.16.5",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "$HOSTRUNTIME_BASE_URL/jre-8-aarch64.tar.xz",
                "arm64" to "$HOSTRUNTIME_BASE_URL/jre-8-aarch64.tar.xz",
                "armeabi-v7a" to "$HOSTRUNTIME_BASE_URL/jre-8-arm.tar.xz",
                "arm" to "$HOSTRUNTIME_BASE_URL/jre-8-arm.tar.xz",
                "x86_64" to "$HOSTRUNTIME_BASE_URL/jre-8-x86_64.tar.xz",
                "x86" to "$HOSTRUNTIME_BASE_URL/jre-8-x86.tar.xz"
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
                "arm64-v8a" to "$HOSTRUNTIME_BASE_URL/jre-17-aarch64.tar.xz",
                "arm64" to "$HOSTRUNTIME_BASE_URL/jre-17-aarch64.tar.xz",
                "armeabi-v7a" to "$HOSTRUNTIME_BASE_URL/jre-17-arm.tar.xz",
                "arm" to "$HOSTRUNTIME_BASE_URL/jre-17-arm.tar.xz",
                "x86_64" to "$HOSTRUNTIME_BASE_URL/jre-17-x86_64.tar.xz",
                "x86" to "$HOSTRUNTIME_BASE_URL/jre-17-x86.tar.xz"
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
                "arm64-v8a" to "$HOSTRUNTIME_BASE_URL/jre-21-aarch64.tar.xz",
                "arm64" to "$HOSTRUNTIME_BASE_URL/jre-21-aarch64.tar.xz",
                "armeabi-v7a" to "$HOSTRUNTIME_BASE_URL/jre-21-arm.tar.xz",
                "arm" to "$HOSTRUNTIME_BASE_URL/jre-21-arm.tar.xz",
                "x86_64" to "$HOSTRUNTIME_BASE_URL/jre-21-x86_64.tar.xz",
                "x86" to "$HOSTRUNTIME_BASE_URL/jre-21-x86.tar.xz"
            )
        ),
        JdkCatalogItem(
            majorVersion = 25,
            runtimeName = "Internal-25",
            title = "Java 25 (LTS)",
            description = "Next-generation runtime featuring enhanced JIT and ZGC for Minecraft snapshots and future versions.",
            recommendedMc = "Minecraft 26.1+ / Snapshots",
            isLts = true,
            downloadUrls = mapOf(
                "arm64-v8a" to "$HOSTRUNTIME_BASE_URL/jre-25-aarch64.tar.xz",
                "arm64" to "$HOSTRUNTIME_BASE_URL/jre-25-aarch64.tar.xz",
                "armeabi-v7a" to "$HOSTRUNTIME_BASE_URL/jre-25-arm.tar.xz",
                "arm" to "$HOSTRUNTIME_BASE_URL/jre-25-arm.tar.xz",
                "x86_64" to "$HOSTRUNTIME_BASE_URL/jre-25-x86_64.tar.xz"
            )
        )
    )

    fun isJdkInstalled(majorVersion: Int): Boolean {
        return RuntimesManager.getRuntimes().any { it.javaVersion == majorVersion && RuntimesManager.isRuntimeComplete(it.name) }
    }

    suspend fun downloadAndInstallJdk(
        context: Context,
        item: JdkCatalogItem,
        onProgress: (Float) -> Unit = {},
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        val archAndroid = Architecture.archAsStringAndroid(ZLApplication.DEVICE_ARCHITECTURE)
        val archGeneric = Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE)
        val url = item.downloadUrls[archAndroid]
            ?: item.downloadUrls[archGeneric]
            ?: item.downloadUrls["arm64-v8a"]
            ?: item.downloadUrls["arm64"]
            ?: run {
                val err = IllegalStateException("No compatible download URL found for arch $archAndroid")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, err.message, Toast.LENGTH_LONG).show()
                    onError(err)
                }
                return@withContext
            }

        val tempFile = File(context.cacheDir, "jdk_${item.majorVersion}_$archGeneric.tar.xz")

        try {
            _downloadingVersions.value = _downloadingVersions.value + (item.majorVersion to 0f)

            val request = Request.Builder()
                .url(url)
                .build()

            DOWNLOAD_OKHTTP_CLIENT.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val msg = if (code == 404) {
                        "Java runtime download failed (HTTP 404)"
                    } else {
                        "Java runtime download failed (HTTP $code)"
                    }
                    throw IOException(msg)
                }
                val body = response.body
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

            if (!RuntimesManager.isRuntimeComplete(item.runtimeName)) {
                throw IOException("Runtime ${item.runtimeName} installation was incomplete!")
            }

            _downloadingVersions.value = _downloadingVersions.value - item.majorVersion
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } catch (e: Throwable) {
            Logger.error(TAG, "Failed to download and install JDK ${item.majorVersion}", e)
            _downloadingVersions.value = _downloadingVersions.value - item.majorVersion
            // Delete broken partial runtime folder if left
            RuntimesManager.removeRuntime(item.runtimeName)
            withContext(Dispatchers.Main) {
                val errorMsg = if (e.message?.contains("404") == true) {
                    "Java runtime download failed (HTTP 404)"
                } else {
                    e.message ?: "Java runtime download failed"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onError(e)
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
