package com.sednium.bladelauncher.game.version.download

import com.sednium.bladelauncher.game.versioninfo.models.GameManifest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterTest {

    @Test
    fun lwjglCoreAndSdlClassesAreKeptForSdlVersions() {
        assertFalse(library("org.lwjgl:lwjgl:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
        assertFalse(library("org.lwjgl:lwjgl-sdl:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
    }

    @Test
    fun otherLwjglModulesRemainFilteredForSdlVersions() {
        assertTrue(library("org.lwjgl:lwjgl-opengl:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
        assertTrue(library("org.lwjgl:lwjgl-openal:3.4.2").filterLibrary(allowLwjglSdlClasses = true))
    }

    @Test
    fun lwjglDesktopNativesRemainFiltered() {
        assertTrue(library("org.lwjgl:lwjgl-sdl:3.4.2:natives-linux").filterLibrary(allowLwjglSdlClasses = true))
        assertTrue(library("org.lwjgl:lwjgl-sdl:3.4.2:natives-windows-arm64").filterLibrary(allowLwjglSdlClasses = true))
    }

    @Test
    fun bundledLwjglRemainsTheDefault() {
        assertTrue(library("org.lwjgl:lwjgl:3.4.2").filterLibrary())
        assertTrue(library("org.lwjgl:lwjgl-opengl:3.4.2").filterLibrary())
    }

    @Test
    fun unrelatedLibrariesAreKept() {
        assertFalse(library("com.example:org.lwjgl.helper:1.0.0").filterLibrary())
    }

    @Test
    fun sdlBindingSelectsCurrentLwjglClasses() {
        assertTrue(
            listOf(
                library("org.lwjgl:lwjgl:3.4.2"),
                library("org.lwjgl:lwjgl-sdl:3.4.2")
            ).usesLwjglSdl()
        )
        assertFalse(listOf(library("org.lwjgl:lwjgl-glfw:3.3.6")).usesLwjglSdl())
    }

    private fun library(coordinates: String) = GameManifest.Library().apply {
        name = coordinates
    }
}
