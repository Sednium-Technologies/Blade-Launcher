/*
 * Blade Launcher
 * Copyright (C) 2026 Sednium Technologies and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sednium.bladelauncher.utils.device

import com.sednium.bladelauncher.game.multirt.Runtime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ArchitectureFallbackTest {

    @Test
    fun testResolveFallbackJavaArch_arm64() {
        val arch = Architecture.resolveFallbackJavaArch(arrayOf("arm64-v8a"))
        assertEquals("aarch64", arch)
        assertEquals(Architecture.ARCH_ARM64, Architecture.archAsInt(arch))
    }

    @Test
    fun testResolveFallbackJavaArch_arm32() {
        val arch = Architecture.resolveFallbackJavaArch(arrayOf("armeabi-v7a", "armeabi"))
        assertEquals("aarch32/arm", arch)
        assertEquals(Architecture.ARCH_ARM, Architecture.archAsInt(arch))
    }

    @Test
    fun testResolveFallbackJavaArch_x86_64() {
        val arch = Architecture.resolveFallbackJavaArch(arrayOf("x86_64"))
        assertEquals("amd64/x86_64", arch)
        assertEquals(Architecture.ARCH_X86_64, Architecture.archAsInt(arch))
    }

    @Test
    fun testResolveFallbackJavaArch_x86() {
        val arch = Architecture.resolveFallbackJavaArch(arrayOf("x86"))
        assertEquals("i386/i486/i586", arch)
        assertEquals(Architecture.ARCH_X86, Architecture.archAsInt(arch))
    }

    @Test
    fun testArchAsInt_allVariations() {
        assertEquals(Architecture.ARCH_ARM64, Architecture.archAsInt("aarch64"))
        assertEquals(Architecture.ARCH_ARM64, Architecture.archAsInt("arm64"))
        assertEquals(Architecture.ARCH_ARM, Architecture.archAsInt("aarch32"))
        assertEquals(Architecture.ARCH_ARM, Architecture.archAsInt("arm"))
        assertEquals(Architecture.ARCH_X86_64, Architecture.archAsInt("amd64"))
        assertEquals(Architecture.ARCH_X86_64, Architecture.archAsInt("x86_64"))
        assertEquals(Architecture.ARCH_X86, Architecture.archAsInt("i386"))
        assertEquals(Architecture.ARCH_X86, Architecture.archAsInt("i586"))
        assertEquals(Architecture.ARCH_X86, Architecture.archAsInt("x86"))
        assertEquals(Architecture.UNSUPPORTED_ARCH, Architecture.archAsInt("unknown-arch"))
    }

    @Test
    fun testRuntimeArchNotNullByDefault() {
        val runtime = Runtime("Internal-8")
        assertNotNull("Runtime arch must never be null", runtime.arch)
        assertNotEquals(Architecture.UNSUPPORTED_ARCH, Architecture.archAsInt(runtime.arch))
    }
}
