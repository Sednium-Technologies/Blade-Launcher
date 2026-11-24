/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.sednium.bladelauncher.components.jre

import android.content.Context
import android.content.res.AssetManager
import com.sednium.bladelauncher.ZLApplication
import com.sednium.bladelauncher.components.AbstractUnpackTask
import com.sednium.bladelauncher.components.InstallableItem
import com.sednium.bladelauncher.game.multirt.RuntimesManager
import com.sednium.bladelauncher.utils.device.Architecture
import com.sednium.bladelauncher.utils.file.readString
import com.sednium.bladelauncher.utils.logging.Logger

private const val TAG = "UnpackJreTask"

class UnpackJreTask(
    private val context: Context,
    private val jre: Jre
) : AbstractUnpackTask() {
    private lateinit var assetManager: AssetManager
    private lateinit var launcherRuntimeVersion: String
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            assetManager = context.assets
            if (!isJreArchSupported()) {
                //当前设备架构不支持使用这个环境
                isCheckFailed = true
                return@runCatching
            }
            launcherRuntimeVersion = runCatching {
                assetManager.open(jre.jrePath + "/version").use { it.readString() }
            }.getOrDefault("1")
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to init jre version. assetsPath=${jre.jrePath}/version", e)
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun checkState(): InstallableItem.State {
        if (isCheckFailed) return InstallableItem.State.NOT_EXISTS

        return runCatching {
            val isComplete = RuntimesManager.isRuntimeComplete(jre.jreName)
            val installedRuntimeVersion = RuntimesManager.loadInternalRuntimeVersion(jre.jreName)

            when {
                // Not installed or corrupted/incomplete (e.g. arch null or missing binaries)
                installedRuntimeVersion == null || !isComplete -> {
                    if (!isComplete && RuntimesManager.getRuntimeDir(jre.jreName).exists()) {
                        Logger.warning(TAG, "Runtime ${jre.jreName} exists on disk but is incomplete or has invalid metadata. Deleting for fresh unpack.")
                        RuntimesManager.removeRuntime(jre.jreName)
                    }
                    InstallableItem.State.NOT_STARTED
                }
                launcherRuntimeVersion != installedRuntimeVersion -> InstallableItem.State.PENDING
                else -> InstallableItem.State.FINISHED
            }
        }.onFailure { e ->
            Logger.error("CheckJre", "An exception occurred while detecting the Java Runtime.", e)
            RuntimesManager.removeRuntime(jre.jreName)
        }.getOrElse {
            // Check failed, require clean reinstallation
            InstallableItem.State.NOT_STARTED
        }
    }

    private fun isJreArchSupported(): Boolean {
        return runCatching {
            val archPack = "${jre.jrePath}/${getRuntimeByArch()}"
            val universalPack = "${jre.jrePath}/universal.tar.xz"
            // Directly test opening asset streams instead of relying on AssetManager.list(),
            // which can return empty on Android for APK directories without explicit zip folder entries.
            assetManager.open(archPack).close()
            assetManager.open(universalPack).close()
            Logger.info(TAG, "Device environment supported for ${jre.jrePath}: $archPack and $universalPack are available")
            true
        }.getOrElse { e ->
            Logger.warning(TAG, "Device requires environment ${jre.jrePath}/${getRuntimeByArch()} but assets not found in APK", e)
            false
        }
    }

    private fun getRuntimeByArch() = "bin-" + Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE) + ".tar.xz"

    override suspend fun run() {
        runCatching {
            RuntimesManager.installRuntimeBinPack(
                universalFileInputStream = assetManager.open(jre.jrePath + "/universal.tar.xz"),
                platformBinsInputStream = assetManager.open(jre.jrePath + "/" + getRuntimeByArch()),
                name = jre.jreName,
                binPackVersion = launcherRuntimeVersion,
                updateProgress = { textRes, textArgs ->
                    updateMessage(context.getString(textRes, *textArgs))
                }
            )
            RuntimesManager.postPrepare(jre.jreName)

            if (!RuntimesManager.isRuntimeComplete(jre.jreName)) {
                throw java.io.IOException("Internal JRE ${jre.jreName} unpacked but completeness verification failed (bin/java or arch missing)!")
            }
        }.onFailure {
            Logger.error("UnpackJre", "Internal JRE unpack failed", it)
            RuntimesManager.removeRuntime(jre.jreName)
        }.getOrThrow()
    }
}