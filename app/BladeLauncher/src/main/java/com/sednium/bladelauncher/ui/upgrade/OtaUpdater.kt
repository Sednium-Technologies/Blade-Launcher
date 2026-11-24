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

package com.sednium.bladelauncher.ui.upgrade

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import com.sednium.bladelauncher.path.DOWNLOAD_OKHTTP_CLIENT
import com.sednium.bladelauncher.upgrade.RemoteData
import com.sednium.bladelauncher.utils.device.Architecture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@Keep
sealed interface OtaDownloadState {
    data object Idle : OtaDownloadState
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speedBytesPerSec: Long
    ) : OtaDownloadState {
        val progress: Float
            get() = if (totalBytes > 0L) (bytesDownloaded.toFloat() / totalBytes).coerceIn(0f, 1f) else -1f
    }
    data class ReadyToInstall(val apkFile: File) : OtaDownloadState
    data class Error(val message: String) : OtaDownloadState
}

object OtaUpdater {

    fun getOtaApkFile(context: Context): File {
        val otaDir = File(context.cacheDir, "ota")
        if (!otaDir.exists()) otaDir.mkdirs()
        return File(otaDir, "BladeLauncher-update.apk")
    }

    fun resolveTargetApk(data: RemoteData): RemoteData.RemoteFile? {
        val deviceArch = runCatching { Architecture.getDeviceArchitecture() }.getOrElse {
            Architecture.ARCH_ARM64
        }
        val targetArch = when (deviceArch) {
            Architecture.ARCH_ARM64 -> RemoteData.RemoteFile.Arch.ARM64
            Architecture.ARCH_ARM -> RemoteData.RemoteFile.Arch.ARM
            Architecture.ARCH_X86_64 -> RemoteData.RemoteFile.Arch.X86_64
            Architecture.ARCH_X86 -> RemoteData.RemoteFile.Arch.X86
            else -> RemoteData.RemoteFile.Arch.ALL
        }
        return data.files.find { it.arch == targetArch }
            ?: data.files.find { it.arch == RemoteData.RemoteFile.Arch.ALL }
            ?: data.files.firstOrNull()
    }

    suspend fun downloadApk(
        context: Context,
        file: RemoteData.RemoteFile,
        onStateChange: (OtaDownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val targetFile = getOtaApkFile(context)
        if (targetFile.exists()) targetFile.delete()

        try {
            onStateChange(OtaDownloadState.Downloading(0L, file.size.takeIf { it > 0 } ?: -1L, 0L))

            val request = Request.Builder()
                .url(file.uri)
                .build()

            DOWNLOAD_OKHTTP_CLIENT.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Download failed with HTTP ${response.code}")
                }
                val body = response.body
                val totalLength = if (file.size > 0L) file.size else body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(16384)
                        var bytesRead = 0
                        var downloaded: Long = 0
                        var lastSpeedTime = System.currentTimeMillis()
                        var lastDownloaded = 0L
                        var currentSpeed = 0L

                        while (coroutineContext.isActive && input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloaded += bytesRead

                            val now = System.currentTimeMillis()
                            val delta = now - lastSpeedTime
                            if (delta >= 500) {
                                currentSpeed = (downloaded - lastDownloaded) * 1000 / delta
                                lastSpeedTime = now
                                lastDownloaded = downloaded
                                onStateChange(
                                    OtaDownloadState.Downloading(
                                        bytesDownloaded = downloaded,
                                        totalBytes = totalLength,
                                        speedBytesPerSec = currentSpeed
                                    )
                                )
                            }
                        }
                        output.flush()
                    }
                }
            }

            if (!coroutineContext.isActive) {
                targetFile.delete()
                onStateChange(OtaDownloadState.Idle)
                return@withContext
            }

            onStateChange(OtaDownloadState.ReadyToInstall(targetFile))
        } catch (c: CancellationException) {
            targetFile.delete()
            onStateChange(OtaDownloadState.Idle)
            throw c
        } catch (e: Throwable) {
            targetFile.delete()
            onStateChange(OtaDownloadState.Error(e.localizedMessage ?: e.toString()))
        }
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openUnknownAppSourcesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            openUnknownAppSourcesSettings(context)
            return false
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
        return true
    }
}
