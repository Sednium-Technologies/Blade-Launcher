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

package com.sednium.bladelauncher.game.multirt

import android.content.Context
import android.system.Os
import com.sednium.bladelauncher.R
import com.sednium.bladelauncher.components.jre.Jre
import com.sednium.bladelauncher.components.jre.UnpackJreTask
import com.sednium.bladelauncher.path.PathManager
import com.sednium.bladelauncher.utils.device.Architecture
import com.sednium.bladelauncher.utils.file.child
import com.sednium.bladelauncher.utils.file.ensureDirectory
import com.sednium.bladelauncher.utils.file.ensureParentDirectory
import com.sednium.bladelauncher.utils.logging.Logger
import com.sednium.bladelauncher.utils.math.findNearestPositive
import com.sednium.bladelauncher.utils.string.compareVersion
import com.sednium.bladelauncher.utils.string.extractUntilCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "RuntimesManager"

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/multirt/MultiRTUtils.java)
 */
object RuntimesManager {
    private val cache = ConcurrentHashMap<String, Runtime>()

    val RUNTIME_FOLDER: File
        get() = runCatching { PathManager.DIR_MULTIRT }.getOrElse {
            File(System.getProperty("java.io.tmpdir"), "bladelauncher_runtimes").apply { mkdirs() }
        }
    private const val JAVA_VERSION_STR: String = "JAVA_VERSION=\""
    private const val OS_ARCH_STR: String = "OS_ARCH=\""

    fun getRuntimeDir(name: String): File = RUNTIME_FOLDER.child(name)

    fun isRuntimeComplete(name: String): Boolean {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists() || !dest.isDirectory) return false
        val releaseFile = File(dest, "release")
        val javaBin = File(dest, "bin/java")
        val libDir = File(dest, "lib")
        if (!releaseFile.exists() && !javaBin.exists()) return false
        if (!libDir.exists() && !javaBin.exists()) return false

        val runtime = loadRuntime(name)
        if (runtime.arch.isNullOrBlank()) return false
        if (runtime.versionString.isNullOrBlank() && !javaBin.exists()) return false
        return true
    }

    fun flattenNestedRuntimeDir(dest: File) {
        if (!dest.exists() || !dest.isDirectory) return
        val releaseFile = File(dest, "release")
        val binDir = File(dest, "bin")
        if (releaseFile.exists() && binDir.exists()) {
            return
        }

        val candidates = dest.listFiles()?.filter { it.isDirectory } ?: return
        val nestedJreDir = candidates.firstOrNull { sub ->
            File(sub, "release").exists() || File(sub, "bin/java").exists() || File(sub, "bin").exists()
        } ?: return

        Logger.info(TAG, "Flattening nested runtime directory: ${nestedJreDir.name} -> ${dest.name}")
        val files = nestedJreDir.listFiles() ?: return
        for (file in files) {
            val target = File(dest, file.name)
            if (target.exists()) {
                if (target.isDirectory) FileUtils.deleteDirectory(target) else target.delete()
            }
            if (!file.renameTo(target)) {
                if (file.isDirectory) {
                    FileUtils.copyDirectory(file, target)
                    FileUtils.deleteDirectory(file)
                } else {
                    FileUtils.copyFile(file, target)
                    file.delete()
                }
            }
        }
        nestedJreDir.delete()
    }

    fun getRuntimes(forceLoad: Boolean = false): List<Runtime> {
        if (!RUNTIME_FOLDER.exists()) {
            Logger.warning(TAG, "Runtime directory not found: ${RUNTIME_FOLDER.absolutePath}")
            return emptyList()
        }

        return RUNTIME_FOLDER.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { loadRuntime(it.name, forceLoad = forceLoad) }
            ?.sortedWith { o1, o2 ->
                val thisVer = o1.versionString ?: o1.name
                -thisVer.compareVersion(o2.versionString ?: o2.name)
            }
            ?: throw IllegalStateException("Failed to access runtime directory")
    }

    fun getExactJreName(majorVersion: Int): String? {
        return getRuntimes().firstOrNull { it.javaVersion == majorVersion }?.name
    }

    fun getNearestJreName(majorVersion: Int): String? {
        return findNearestPositive(majorVersion, getRuntimes()) { it.javaVersion }?.value?.name
    }

    fun forceReload(name: String): Runtime {
        cache.remove(name)
        return loadRuntime(name)
    }

    fun loadRuntime(name: String, forceLoad: Boolean = false): Runtime {
        return cache[name]?.takeIf { !forceLoad } ?: run {
            val runtimeDir = File(RUNTIME_FOLDER, name)
            if (!runtimeDir.exists()) {
                val fallbackArch = Architecture.resolveFallbackJavaArch()
                return Runtime(
                    name = name,
                    versionString = null,
                    arch = fallbackArch,
                    javaVersion = 0,
                    isProvidedByLauncher = Jre.entries.any { it.jreName == name },
                    isJDK8 = false
                ).also { cache[name] = it }
            }

            flattenNestedRuntimeDir(runtimeDir)

            val releaseFile = File(runtimeDir, "release")
            val fallbackArch = Architecture.resolveFallbackJavaArch()

            if (!releaseFile.exists()) {
                val isJdk8 = isJDK8(runtimeDir.absolutePath)
                val javaBin = File(runtimeDir, "bin/java")
                val hasBinaries = javaBin.exists()
                return Runtime(
                    name = name,
                    versionString = if (hasBinaries && (name.contains("8") || isJdk8)) "1.8.0" else null,
                    arch = fallbackArch,
                    javaVersion = if (hasBinaries && (name.contains("8") || isJdk8)) 8 else 0,
                    isProvidedByLauncher = Jre.entries.any { it.jreName == name },
                    isJDK8 = isJdk8
                ).also { cache[name] = it }
            }

            runCatching {
                val content = releaseFile.readText()
                val javaVersion = content.extractUntilCharacter(JAVA_VERSION_STR, '"')
                val osArch = content.extractUntilCharacter(OS_ARCH_STR, '"') ?: fallbackArch

                if (javaVersion != null) {
                    val versionParts = javaVersion.split('.')
                    val majorVersion = if (versionParts.first() == "1") {
                        versionParts.getOrNull(1)?.toIntOrNull() ?: 0
                    } else {
                        versionParts.first().toIntOrNull() ?: 0
                    }

                    Runtime(
                        name = name,
                        versionString = javaVersion,
                        arch = osArch,
                        javaVersion = majorVersion,
                        isProvidedByLauncher = Jre.entries.any { it.jreName == name },
                        isJDK8 = isJDK8(runtimeDir.absolutePath)
                    )
                } else {
                    Runtime(
                        name = name,
                        versionString = null,
                        arch = fallbackArch,
                        javaVersion = 0,
                        isProvidedByLauncher = Jre.entries.any { it.jreName == name },
                        isJDK8 = isJDK8(runtimeDir.absolutePath)
                    )
                }
            }.onFailure { e ->
                Logger.error(TAG, "Failed to load runtime $name", e)
            }.getOrElse {
                Runtime(
                    name = name,
                    versionString = null,
                    arch = fallbackArch,
                    javaVersion = 0,
                    isProvidedByLauncher = Jre.entries.any { it.jreName == name },
                    isJDK8 = isJDK8(runtimeDir.absolutePath)
                )
            }.also { cache[name] = it }
        }
    }

    @Throws(IOException::class)
    suspend fun installRuntime(
        nativeLibDir: String,
        inputStream: InputStream,
        name: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            uncompressTarXZ(inputStream, dest, updateProgress)
            flattenNestedRuntimeDir(dest)
            unpack200(nativeLibDir, dest.absolutePath)
            loadRuntime(name).also { runtime ->
                postPrepare(runtime)
            }
            if (!isRuntimeComplete(name)) {
                throw IOException("Runtime $name unpacked but completeness check failed!")
            }
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    @Throws(IOException::class)
    suspend fun postPrepare(name: String) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists()) return@withContext
        val runtime = loadRuntime(name)
        postPrepare(runtime)
    }

    @Throws(IOException::class)
    suspend fun postPrepare(runtime: Runtime) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(runtime.name)
        if (!dest.exists()) return@withContext
        var libFolder = "lib"

        val arch = runtime.arch ?: Architecture.resolveFallbackJavaArch()
        arch.split("/").forEach { subArch ->
            if (dest.child(libFolder, subArch).exists()) {
                libFolder += "/$subArch"
            }
        }

        val isJDK8 = isJDK8(dest.absolutePath)
        if (isJDK8) {
            libFolder = "/jre$libFolder"
        }

        val ftIn = dest.child(libFolder, "libfreetype.so.6")
        val ftOut = dest.child(libFolder, "libfreetype.so")
        if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) {
            if (!ftIn.renameTo(ftOut)) throw IOException("Failed to rename freetype")
        }

        val ft2In = dest.child(libFolder, "libfreetype.so")
        if (isJDK8 && ft2In.exists()) {
            ft2In.renameTo(ftOut)
        }

        val localXawtLib = File(PathManager.DIR_NATIVE_LIB, "libawt_xawt.so")
        val targetXawtLib = dest.child(libFolder, "libawt_xawt.so")
        if (targetXawtLib.exists()) targetXawtLib.delete()
        if (localXawtLib.exists()) {
            FileUtils.copyFile(localXawtLib, targetXawtLib)
        }
    }

    /**
     * Ensures all JRE environments bundled in APK assets for the current architecture
     * (especially Internal-25) are unpacked and registered into RUNTIME_FOLDER.
     */
    suspend fun ensureBundledRuntimes(context: Context) = withContext(Dispatchers.IO) {
        Jre.entries.forEach { jre ->
            val task = UnpackJreTask(context, jre)
            if (!task.isCheckFailed()) {
                val isComplete = isRuntimeComplete(jre.jreName)
                if (!isComplete) {
                    Logger.info(TAG, "Bundled runtime ${jre.jreName} not complete on disk. Unpacking now...")
                    runCatching {
                        task.run()
                        Logger.info(TAG, "Bundled runtime ${jre.jreName} successfully unpacked!")
                    }.onFailure { e ->
                        Logger.error(TAG, "Failed to unpack bundled runtime ${jre.jreName}", e)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun installRuntimeBinPack(
        universalFileInputStream: InputStream,
        platformBinsInputStream: InputStream,
        name: String,
        binPackVersion: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            installRuntimeNoRemove(universalFileInputStream, dest, updateProgress)
            installRuntimeNoRemove(platformBinsInputStream, dest, updateProgress)
            flattenNestedRuntimeDir(dest)

            unpack200(PathManager.DIR_NATIVE_LIB, dest.absolutePath)

            val versionFile = File(dest, "version")
            versionFile.writeText(binPackVersion)

            forceReload(name)
            if (!isRuntimeComplete(name)) {
                throw IOException("Internal runtime $name installed but completeness check failed!")
            }
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    fun loadInternalRuntimeVersion(name: String): String? {
        val versionFile = RUNTIME_FOLDER.child(name, "version")
        try {
            return if (versionFile.exists()) {
                versionFile.readText()
            } else {
                null
            }
        } catch (_: IOException) {
            return null
        }
    }

    @Throws(IOException::class)
    fun removeRuntime(name: String) {
        val dest: File = RUNTIME_FOLDER.child(name).takeIf { it.exists() } ?: return
        FileUtils.deleteDirectory(dest)
        cache.remove(name)
    }

    fun getRuntimeHome(name: String): File {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists() || !isRuntimeComplete(name)) {
            Logger.warning(TAG, "Runtime $name is incomplete, missing, or corrupted. Cleaning up directory...")
            removeRuntime(name)
            throw IOException("Selected Java runtime $name is incomplete or corrupted! It has been removed. Please relaunch to auto-repair internal runtimes or reinstall from Java Manager.")
        }

        return dest
    }

    /**
     * Unpacks all .pack files into .jar Serves only for java 8, as java 9 brought project jigsaw
     * @param nativeLibraryDir The native lib path, required to execute the unpack200 binary
     * @param runtimePath The path to the runtime to walk into
     */
    private suspend fun unpack200(
        nativeLibraryDir: String,
        runtimePath: String
    ) = withContext(Dispatchers.Default) {
            val basePath = File(runtimePath)
            val files: Collection<File> = FileUtils.listFiles(basePath, arrayOf("pack"), true)

            val workDir = File(nativeLibraryDir)
            val processBuilder = ProcessBuilder().directory(workDir)

            files.forEach { jarFile ->
                ensureActive()
                runCatching {
                    val destPath = jarFile.absolutePath.replace(".pack", "")
                    processBuilder.command(
                        "./libunpack200.so",
                        "-r",
                        jarFile.absolutePath,
                        destPath
                    ).start().apply {
                        waitFor()
                    }
                }.onFailure { e ->
                    if (e is IOException) {
                        Logger.error(TAG, "Failed to unpack the runtime!", e)
                    } else throw e
                }
            }
        }

    @Throws(IOException::class)
    private suspend fun installRuntimeNoRemove(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        uncompressTarXZ(inputStream, dest, updateProgress)
        inputStream.close()
    }

    @Throws(IOException::class)
    private suspend fun uncompressTarXZ(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        dest.ensureDirectory()
        val buffer = ByteArray(8192)

        TarArchiveInputStream(XZCompressorInputStream(inputStream)).use { tarIn ->
            generateSequence { tarIn.nextEntry }.forEach { tarEntry ->
                ensureActive()
                val tarEntryName = tarEntry.name
                updateProgress(R.string.generic_unpacking, arrayOf(tarEntryName))

                val destPath = File(dest, tarEntryName).ensureParentDirectory()

                when {
                    tarEntry.isSymbolicLink -> try {
                        Os.symlink(tarEntry.linkName, tarEntryName)
                    } catch (e: Throwable) {
                        Logger.error(TAG, "Exception occurred while creating symbolic link", e)
                    }

                    tarEntry.isDirectory -> destPath.ensureDirectory()
                    !destPath.exists() || destPath.length() != tarEntry.size ->
                        FileOutputStream(destPath).use { os ->
                            IOUtils.copyLarge(tarIn, os, buffer)
                        }
                }
            }
        }
    }

    fun isJDK8(runtimeDir: String): Boolean {
        return File(runtimeDir, "jre").exists() && File(runtimeDir, "bin/javac").exists()
    }
}