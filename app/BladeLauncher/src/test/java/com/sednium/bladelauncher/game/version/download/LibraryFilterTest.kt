package com.sednium.bladelauncher.game.version.download

import com.sednium.bladelauncher.game.versioninfo.models.GameManifest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterTest {

    @Test
    fun lwjglModulesAreFilteredInFavorOfBundledComponents() {
        assertTrue(library("org.lwjgl:lwjgl:3.4.2").filterLibrary())
        assertTrue(library("org.lwjgl:lwjgl-sdl:3.4.2").filterLibrary())
        assertTrue(library("org.lwjgl:lwjgl-opengl:3.4.2").filterLibrary())
        assertTrue(library("org.lwjgl:lwjgl-openal:3.4.2").filterLibrary())
        assertTrue(library("org.lwjgl:lwjgl-sdl:3.4.2:natives-linux").filterLibrary())
    }

    @Test
    fun legacyPlatformsAreFiltered() {
        assertTrue(library("net.java.jinput:jinput-platform:2.0.5").filterLibrary())
        assertTrue(library("tv.twitch:twitch-platform:5.16").filterLibrary())
    }

    @Test
    fun unrelatedLibrariesAreKept() {
        assertFalse(library("com.google.guava:guava:31.1-jre").filterLibrary())
        assertFalse(library("org.slf4j:slf4j-api:2.0.7").filterLibrary())
    }

    private fun library(coordinates: String) = GameManifest.Library().apply {
        name = coordinates
    }
}
