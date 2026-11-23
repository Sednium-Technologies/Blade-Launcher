package com.sednium.bladelauncher.utils.device

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import com.sednium.bladelauncher.game.plugin.driver.Driver
import com.sednium.bladelauncher.game.plugin.driver.DriverPluginManager
import com.sednium.bladelauncher.game.renderer.RendererInterface
import com.sednium.bladelauncher.game.renderer.Renderers
import com.sednium.bladelauncher.game.renderer.renderers.NGGL4ESRenderer
import com.sednium.bladelauncher.game.renderer.renderers.VulkanZinkRenderer
import com.sednium.bladelauncher.utils.logging.Logger
import java.io.File

enum class GpuFamily {
    QUALCOMM_ADRENO,
    ARM_MALI,
    POWERVR,
    INTEL_MESA,
    UNKNOWN
}

object DeviceHardwareProfiler {
    private const val TAG = "HardwareProfiler"
    private const val OVERRIDE_FILE_PATH = "/sdcard/debug_gpu_override.txt"
    private var cachedGpuFamily: GpuFamily? = null
    private var cachedRendererString: String? = null
    private var cachedVendorString: String? = null
    private var isTestingOverride = false
    private var isFileOverrideActive = false

    private fun checkDebugOverride(): Boolean {
        if (isTestingOverride) return true

        val prop = System.getProperty("blade.gpu_override")?.trim()
        if (!prop.isNullOrEmpty()) {
            val parts = prop.split(";", limit = 2)
            val vendor = if (parts.size > 1) parts[0].trim() else ""
            val renderer = if (parts.size > 1) parts[1].trim() else parts[0].trim()
            cachedVendorString = vendor
            cachedRendererString = renderer
            cachedGpuFamily = parseGpuFamily(vendor, renderer)
            return true
        }

        val file = File(OVERRIDE_FILE_PATH)
        if (file.exists()) {
            try {
                val text = file.readText().trim()
                if (text.isNotEmpty()) {
                    val parts = text.split(";", limit = 2)
                    val vendor = if (parts.size > 1) parts[0].trim() else ""
                    val renderer = if (parts.size > 1) parts[1].trim() else parts[0].trim()
                    cachedVendorString = vendor
                    cachedRendererString = renderer
                    cachedGpuFamily = parseGpuFamily(vendor, renderer)
                    isFileOverrideActive = true
                    Logger.info(TAG, "Debug GPU Override active ($OVERRIDE_FILE_PATH)! Detected GPU Family: $cachedGpuFamily (Vendor: $vendor, Renderer: $renderer)")
                    return true
                }
            } catch (e: Throwable) {
                Logger.warning(TAG, "Failed to read debug GPU override file: ${e.message}")
            }
        } else if (isFileOverrideActive) {
            cachedGpuFamily = null
            cachedVendorString = null
            cachedRendererString = null
            isFileOverrideActive = false
            Logger.info(TAG, "Debug GPU Override removed; resetting to hardware probing")
        }
        return false
    }

    @Synchronized
    fun probeGpu() {
        if (checkDebugOverride()) return
        if (cachedGpuFamily != null) return

        try {
            val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                cachedGpuFamily = GpuFamily.UNKNOWN
                return
            }

            if (!EGL14.eglInitialize(eglDisplay, null, 0, null, 0)) {
                cachedGpuFamily = GpuFamily.UNKNOWN
                return
            }

            val eglAttributes = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (!EGL14.eglChooseConfig(eglDisplay, eglAttributes, 0, configs, 0, 1, numConfigs, 0) || numConfigs[0] == 0) {
                EGL14.eglTerminate(eglDisplay)
                cachedGpuFamily = GpuFamily.UNKNOWN
                return
            }

            val config = configs[0] ?: run {
                EGL14.eglTerminate(eglDisplay)
                cachedGpuFamily = GpuFamily.UNKNOWN
                return
            }

            val contextAttributes = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val context = EGL14.eglCreateContext(eglDisplay, config, EGL14.EGL_NO_CONTEXT, contextAttributes, 0)
            if (context == EGL14.EGL_NO_CONTEXT) {
                EGL14.eglTerminate(eglDisplay)
                cachedGpuFamily = GpuFamily.UNKNOWN
                return
            }

            if (!EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, context)) {
                EGL14.eglDestroyContext(eglDisplay, context)
                EGL14.eglTerminate(eglDisplay)
                cachedGpuFamily = GpuFamily.UNKNOWN
                return
            }

            val vendor = GLES20.glGetString(GLES20.GL_VENDOR).orEmpty()
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER).orEmpty()
            cachedVendorString = vendor
            cachedRendererString = renderer

            cachedGpuFamily = parseGpuFamily(vendor, renderer)

            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(eglDisplay, context)
            EGL14.eglTerminate(eglDisplay)

            Logger.info(TAG, "Detected GPU Family: $cachedGpuFamily (Vendor: $vendor, Renderer: $renderer)")
        } catch (e: Throwable) {
            Logger.warning(TAG, "Failed to probe GPU: ${e.message}")
            cachedGpuFamily = GpuFamily.UNKNOWN
        }
    }

    fun parseGpuFamily(vendor: String, renderer: String): GpuFamily {
        val vLower = vendor.lowercase()
        val rLower = renderer.lowercase()

        return when {
            rLower.contains("adreno") || vLower.contains("qualcomm") -> GpuFamily.QUALCOMM_ADRENO
            rLower.contains("mali") || rLower.contains("immortalis") || vLower.contains("arm") -> GpuFamily.ARM_MALI
            rLower.contains("powervr") || rLower.contains("rogue") || rLower.contains("img") || vLower.contains("imagination") || vLower.contains("img") -> GpuFamily.POWERVR
            rLower.contains("intel") || rLower.contains("mesa") || rLower.contains("llvmpipe") || rLower.contains("ranchu") -> GpuFamily.INTEL_MESA
            else -> GpuFamily.UNKNOWN
        }
    }

    fun setGpuForTesting(family: GpuFamily?, vendor: String? = null, renderer: String? = null) {
        cachedGpuFamily = family
        cachedVendorString = vendor
        cachedRendererString = renderer
        isTestingOverride = (family != null)
    }

    fun clearCache() {
        cachedGpuFamily = null
        cachedVendorString = null
        cachedRendererString = null
        isTestingOverride = false
        isFileOverrideActive = false
    }

    fun getGpuFamily(): GpuFamily {
        if (checkDebugOverride()) {
            return cachedGpuFamily ?: GpuFamily.UNKNOWN
        }
        if (cachedGpuFamily == null) {
            probeGpu()
        }
        return cachedGpuFamily ?: GpuFamily.UNKNOWN
    }

    fun resolveRenderer(context: Context, requested: String): RendererInterface {
        val compatible = Renderers.getCompatibleRenderers(context).second
        if (requested.isNotEmpty() && requested != "auto") {
            val userRenderer = compatible.find { it.getUniqueIdentifier() == requested || it.getRendererId() == requested }
            if (userRenderer != null) return userRenderer
        }

        val candidates = compatible.filter { it.getUniqueIdentifier() != "auto" }.ifEmpty { compatible }
        val hasVulkan = checkVulkanSupport(context.packageManager)
        val gpu = getGpuFamily()

        return when {
            gpu == GpuFamily.QUALCOMM_ADRENO && hasVulkan -> {
                candidates.find { it is VulkanZinkRenderer } ?: NGGL4ESRenderer
            }
            gpu == GpuFamily.POWERVR -> NGGL4ESRenderer
            gpu == GpuFamily.ARM_MALI -> NGGL4ESRenderer
            gpu == GpuFamily.INTEL_MESA -> NGGL4ESRenderer
            else -> candidates.firstOrNull() ?: NGGL4ESRenderer
        }
    }

    @JvmOverloads
    fun resolveDriver(context: Context? = null, requested: String): Driver {
        val driverList = DriverPluginManager.getDriverList()
        val candidates = driverList.filter { it.id != "auto" }.ifEmpty { driverList }
        if (requested.isNotEmpty() && requested != "auto") {
            val userDriver = candidates.find { it.id == requested || it.name == requested }
                ?: driverList.find { it.id == requested || it.name == requested }
            if (userDriver != null) return userDriver
            if (candidates.isEmpty()) {
                return Driver(
                    id = requested,
                    appName = "",
                    appVersion = "",
                    name = requested,
                    path = "",
                    isLauncher = true
                )
            }
        }

        if (candidates.isEmpty()) {
            val isAdreno = (getGpuFamily() == GpuFamily.QUALCOMM_ADRENO)
            return Driver(
                id = if (isAdreno) "default turnip" else "default",
                appName = "",
                appVersion = "",
                name = if (isAdreno) "Turnip" else "Default",
                path = "",
                isLauncher = true
            )
        }

        val gpu = getGpuFamily()
        return when (gpu) {
            GpuFamily.QUALCOMM_ADRENO -> {
                candidates.find { it.name.contains("Turnip", ignoreCase = true) } ?: candidates.first()
            }
            GpuFamily.ARM_MALI, GpuFamily.POWERVR, GpuFamily.INTEL_MESA -> {
                candidates.find { !it.name.contains("Turnip", ignoreCase = true) } ?: candidates.first()
            }
            else -> candidates.first()
        }
    }

    fun getOptimalChunkDistance(): Int {
        return when (getGpuFamily()) {
            GpuFamily.POWERVR -> 4
            GpuFamily.ARM_MALI -> 6
            GpuFamily.QUALCOMM_ADRENO -> 10
            GpuFamily.INTEL_MESA -> 8
            GpuFamily.UNKNOWN -> 6
        }
    }
}
