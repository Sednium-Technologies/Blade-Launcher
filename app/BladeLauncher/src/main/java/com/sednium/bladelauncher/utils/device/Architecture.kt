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

package com.sednium.bladelauncher.utils.device

import android.os.Build

/**
 * [from Architecture.java](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Architecture.java)
 */
object Architecture {
    const val UNSUPPORTED_ARCH = -1
    const val ARCH_ARM64 = 0x1
    const val ARCH_ARM = 0x2
    const val ARCH_X86 = 0x4
    const val ARCH_X86_64 = 0x8

    const val ADDRESS_SPACE_LIMIT_32_BIT: Long = 0xbfffffffL
    const val ADDRESS_SPACE_LIMIT_64_BIT: Long = 0x7fffffffffL

    fun getAddressSpaceLimit() = if (is64BitsDevice) ADDRESS_SPACE_LIMIT_64_BIT else ADDRESS_SPACE_LIMIT_32_BIT

    val is64BitsDevice: Boolean
        get() = runCatching { Build.SUPPORTED_64_BIT_ABIS?.isNotEmpty() == true }.getOrElse {
            System.getProperty("os.arch")?.contains("64") == true
        }

    val is32BitsDevice: Boolean
        get() = !is64BitsDevice

    /**
     * Resolves the architecture this specific APK binary was compiled for (BuildKeys.BUILD_ARCH).
     * If compiled for "arm", returns ARCH_ARM (32-bit).
     */
    fun getRunningAppArch(): Int {
        val buildArch = runCatching { com.sednium.bladelauncher.BuildKeys.BUILD_ARCH }.getOrNull()?.lowercase()?.trim()
        return when (buildArch) {
            "arm" -> ARCH_ARM
            "arm64" -> ARCH_ARM64
            "x86_64" -> ARCH_X86_64
            "x86" -> ARCH_X86
            else -> UNSUPPORTED_ARCH
        }
    }

    /**
     * The primary ABI reported by the Android OS environment.
     */
    fun getDevicePrimaryAbi(): String {
        return runCatching {
            Build.SUPPORTED_ABIS?.firstOrNull { it.isNotBlank() }
        }.getOrNull() ?: System.getProperty("os.arch").orEmpty()
    }

    /**
     * Accurately determines the recommended architecture for OTA/APK updates.
     * Takes into account the current APK build architecture, 32-bit userspace environments,
     * primary Android ABI, and processor architecture.
     */
    fun getRecommendedArch(): Int {
        // 1. If running an architecture-specific APK build, always prefer updating to the same architecture
        val runningArch = getRunningAppArch()
        if (runningArch != UNSUPPORTED_ARCH) {
            return runningArch
        }

        // 2. Inspect the primary ABI reported by the Android OS
        val primaryAbi = getDevicePrimaryAbi().lowercase()
        if (primaryAbi.startsWith("armeabi") || primaryAbi == "arm") {
            return ARCH_ARM
        }
        if (primaryAbi.startsWith("arm64") || primaryAbi.contains("aarch64")) {
            // Check if process is 32-bit on a device with no 64-bit ABI support (e.g. 32-bit Android OS on 64-bit SoC)
            val isProcess64Bit = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    android.os.Process.is64Bit()
                } else {
                    false
                }
            }.getOrNull()
            val supported64 = runCatching { Build.SUPPORTED_64_BIT_ABIS }.getOrNull()
            if (isProcess64Bit == false && (supported64 == null || supported64.isEmpty())) {
                return ARCH_ARM
            }
            return ARCH_ARM64
        }
        if (primaryAbi.contains("x86_64") || primaryAbi.contains("amd64")) {
            return ARCH_X86_64
        }
        if (primaryAbi.contains("x86") || (primaryAbi.startsWith("i") && primaryAbi.endsWith("86"))) {
            return ARCH_X86
        }

        // 3. Fallback to general device architecture
        return getDeviceArchitecture()
    }

    fun getDeviceArchitecture(): Int {
        return if (isx86Device()) {
            if (is64BitsDevice) ARCH_X86_64 else ARCH_X86
        } else {
            if (is64BitsDevice) ARCH_ARM64 else ARCH_ARM
        }
    }

    fun isx86Device(): Boolean {
        val abis = runCatching {
            if (is64BitsDevice) Build.SUPPORTED_64_BIT_ABIS else Build.SUPPORTED_32_BIT_ABIS
        }.getOrNull()
        if (abis != null && abis.isNotEmpty()) {
            val comparedArch = if (is64BitsDevice) ARCH_X86_64 else ARCH_X86
            return abis.any { archAsInt(it) == comparedArch }
        }
        val osArch = System.getProperty("os.arch")?.lowercase() ?: ""
        return osArch.contains("x86") || osArch.contains("amd64") || osArch.contains("i386")
    }

    fun archAsInt(arch: String?): Int {
        val normalizedArch = arch?.lowercase()?.trim()?.replace(" ", "") ?: return UNSUPPORTED_ARCH
        return when {
            normalizedArch.contains("arm64") || normalizedArch.contains("aarch64") -> ARCH_ARM64
            normalizedArch.contains("arm") || normalizedArch.contains("aarch32") -> ARCH_ARM
            normalizedArch.contains("x86_64") || normalizedArch.contains("amd64") -> ARCH_X86_64
            normalizedArch.contains("x86") || (normalizedArch.startsWith("i") && normalizedArch.endsWith("86")) -> ARCH_X86
            else -> UNSUPPORTED_ARCH
        }
    }

    /**
     * Resolves the Java runtime architecture string with fallback to Build.SUPPORTED_ABIS.
     * Maps Android ABIs (arm64-v8a -> aarch64, armeabi-v7a -> aarch32/arm, x86_64 -> amd64/x86_64, x86 -> i386/i486/i586).
     */
    fun resolveFallbackJavaArch(abiList: Array<String>? = runCatching { Build.SUPPORTED_ABIS }.getOrNull()): String {
        abiList?.forEach { abi ->
            val normalized = abi.lowercase().trim()
            when {
                normalized.startsWith("arm64") || normalized.contains("aarch64") -> return "aarch64"
                normalized.startsWith("armeabi") || normalized.contains("arm") || normalized.contains("aarch32") -> return "aarch32/arm"
                normalized.contains("x86_64") || normalized.contains("amd64") -> return "amd64/x86_64"
                normalized.contains("x86") || (normalized.startsWith("i") && normalized.endsWith("86")) -> return "i386/i486/i586"
            }
        }
        val systemArch = System.getProperty("os.arch").orEmpty().lowercase()
        return when {
            systemArch.contains("aarch64") || systemArch.contains("arm64") -> "aarch64"
            systemArch.contains("arm") -> "aarch32/arm"
            systemArch.contains("x86_64") || systemArch.contains("amd64") -> "amd64/x86_64"
            systemArch.contains("x86") || (systemArch.startsWith("i") && systemArch.endsWith("86")) -> "i386/i486/i586"
            else -> runCatching {
                when (getDeviceArchitecture()) {
                    ARCH_ARM64 -> "aarch64"
                    ARCH_ARM -> "aarch32/arm"
                    ARCH_X86_64 -> "amd64/x86_64"
                    ARCH_X86 -> "i386/i486/i586"
                    else -> archAsString(getDeviceArchitecture())
                }
            }.getOrDefault("aarch64")
        }
    }

    fun archAsString(arch: Int): String = when (arch) {
        ARCH_ARM64 -> "arm64"
        ARCH_ARM -> "arm"
        ARCH_X86_64 -> "x86_64"
        ARCH_X86 -> "x86"
        else -> "UNSUPPORTED_ARCH"
    }

    /** Android ABI 目录名 */
    fun archAsStringAndroid(arch: Int): String = when (arch) {
        ARCH_ARM64 -> "arm64-v8a"
        ARCH_ARM -> "armeabi-v7a"
        ARCH_X86_64 -> "x86_64"
        ARCH_X86 -> "x86"
        else -> "UNSUPPORTED_ARCH"
    }
}
