# Auto Driver/Renderer Configuration & Block/Sky Rendering Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement hardware profiling to automatically configure drivers and renderers (Adreno, Mali, PowerVR, Emulator) with manual user override preservation, fix Minecraft 26.3 block and sky rendering (smooth lighting, transparent cutout leaves, clouds, stars), and gate flat cloud patches behind runtime flags in `MioLibPatcher.jar`.

**Architecture:**
- `DeviceHardwareProfiler` probes EGL/GLES & Vulkan to detect GPU family (`QUALCOMM`, `MALI`, `POWERVR`, `INTEL_MESA`, `UNKNOWN`).
- `Renderers.kt` and `DriverPluginManager.kt` support `"auto"` mode as default while strictly preserving explicit user settings in `versionConfig` and `AllSettings`.
- `GameGraphicsAutoTuner` sets optimal default graphics flags (`ao:true`, `cutoutLeaves:true`, `mipmapLevels:4`, `graphicsPreset:"fancy"`) without overwriting user customizations.
- `ShaderFixTransformer.java` inside `MioLibPatcher.jar` checks `BLADE_USE_FLAT_CLOUDS` environment variable: when set (GL4ES), routes clouds to `RenderPipelines.FLAT_CLOUDS` instead of disabling them; when unset (Vulkan Zink), leaves native volumetric clouds intact.

**Tech Stack:** Kotlin, Java, Javassist, Android EGL/GLES, Gradle, Bytecode Instrumentation.

## Global Constraints
- "Auto (Recommended)" must be default for renderer and driver in settings.
- Explicit user configurations in `versionConfig` and `AllSettings` must NOT be overwritten by the auto-profiler.
- `BLADE_USE_FLAT_CLOUDS=1` must be set only for GL4ES/NG-GL4ES; Vulkan Zink must run volumetric clouds natively.
- Maintain MovTery attribution and Saturn launcher branding.

---

### Task 1: Hardware Profiler & Auto Driver/Renderer Detection

**Files:**
- Create: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/utils/device/DeviceHardwareProfiler.kt`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/setting/AllSettings.kt`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/renderer/Renderers.kt`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/plugin/driver/DriverPluginManager.kt`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/launch/GameLauncher.kt`

**Interfaces:**
- `DeviceHardwareProfiler.getGpuFamily(): GpuFamily`
- `DeviceHardwareProfiler.resolveRenderer(context: Context, requested: String): RendererInterface`
- `DeviceHardwareProfiler.resolveDriver(context: Context, requested: String): Driver`
- `DeviceHardwareProfiler.getOptimalChunkDistance(): Int`

- [ ] **Step 1: Implement `DeviceHardwareProfiler.kt`**
Create `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/utils/device/DeviceHardwareProfiler.kt` with EGL probing:
```kotlin
package com.sednium.bladelauncher.utils.device

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.os.Build
import com.sednium.bladelauncher.game.plugin.driver.Driver
import com.sednium.bladelauncher.game.plugin.driver.DriverPluginManager
import com.sednium.bladelauncher.game.renderer.RendererInterface
import com.sednium.bladelauncher.game.renderer.Renderers
import com.sednium.bladelauncher.game.renderer.renderers.NGGL4ESRenderer
import com.sednium.bladelauncher.game.renderer.renderers.VulkanZinkRenderer
import com.sednium.bladelauncher.utils.logging.Logger

enum class GpuFamily {
    QUALCOMM_ADRENO,
    ARM_MALI,
    POWERVR,
    INTEL_MESA,
    UNKNOWN
}

object DeviceHardwareProfiler {
    private const val TAG = "HardwareProfiler"
    private var cachedGpuFamily: GpuFamily? = null
    private var cachedRendererString: String? = null
    private var cachedVendorString: String? = null

    fun probeGpu() {
        if (cachedGpuFamily != null) return

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

        val contextAttributes = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        val context = EGL14.eglCreateContext(eglDisplay, configs[0]!!, EGL14.EGL_NO_CONTEXT, contextAttributes, 0)
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

        val vLower = vendor.lowercase()
        val rLower = renderer.lowercase()

        cachedGpuFamily = when {
            rLower.contains("adreno") || vLower.contains("qualcomm") -> GpuFamily.QUALCOMM_ADRENO
            rLower.contains("mali") || rLower.contains("immortalis") || vLower.contains("arm") -> GpuFamily.ARM_MALI
            rLower.contains("powervr") || rLower.contains("rogue") || rLower.contains("img") || vLower.contains("imagination") -> GpuFamily.POWERVR
            rLower.contains("intel") || rLower.contains("mesa") || rLower.contains("llvmpipe") || rLower.contains("ranchu") -> GpuFamily.INTEL_MESA
            else -> GpuFamily.UNKNOWN
        }

        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroyContext(eglDisplay, context)
        EGL14.eglTerminate(eglDisplay)

        Logger.info(TAG, "Detected GPU Family: $cachedGpuFamily (Vendor: $vendor, Renderer: $renderer)")
    }

    fun getGpuFamily(): GpuFamily {
        probeGpu()
        return cachedGpuFamily ?: GpuFamily.UNKNOWN
    }

    fun resolveRenderer(context: Context, requested: String): RendererInterface {
        val compatible = Renderers.getCompatibleRenderers(context).second
        if (requested.isNotEmpty() && requested != "auto") {
            val userRenderer = compatible.find { it.getUniqueIdentifier() == requested || it.getRendererId() == requested }
            if (userRenderer != null) return userRenderer
        }

        val hasVulkan = checkVulkanSupport(context.packageManager)
        val gpu = getGpuFamily()

        return when {
            gpu == GpuFamily.QUALCOMM_ADRENO && hasVulkan -> {
                compatible.find { it is VulkanZinkRenderer } ?: NGGL4ESRenderer
            }
            gpu == GpuFamily.POWERVR -> NGGL4ESRenderer
            gpu == GpuFamily.ARM_MALI -> NGGL4ESRenderer
            gpu == GpuFamily.INTEL_MESA && hasVulkan -> {
                compatible.find { it is VulkanZinkRenderer } ?: NGGL4ESRenderer
            }
            else -> compatible.firstOrNull() ?: NGGL4ESRenderer
        }
    }

    fun resolveDriver(context: Context, requested: String): Driver {
        val driverList = DriverPluginManager.getDriverList()
        if (requested.isNotEmpty() && requested != "auto") {
            val userDriver = driverList.find { it.id == requested || it.name == requested }
            if (userDriver != null) return userDriver
        }

        val gpu = getGpuFamily()
        return when (gpu) {
            GpuFamily.QUALCOMM_ADRENO -> {
                driverList.find { it.name.contains("Turnip", ignoreCase = true) } ?: driverList.first()
            }
            GpuFamily.ARM_MALI, GpuFamily.POWERVR, GpuFamily.INTEL_MESA -> {
                driverList.find { !it.name.contains("Turnip", ignoreCase = true) } ?: driverList.first()
            }
            else -> driverList.first()
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
```

- [ ] **Step 2: Update `AllSettings.kt` and `Renderers.kt` for "auto" Defaults**
Update default values in `AllSettings.kt`:
```kotlin
val renderer = stringSetting("renderer", "auto")
val vulkanDriver = stringSetting("vulkanDriver", "auto")
```
In `Renderers.kt`:
Update `setCurrentRenderer` to use `DeviceHardwareProfiler.resolveRenderer(context, uniqueIdentifier)`.

- [ ] **Step 3: Update `NGGL4ESRenderer.kt`**
Add `BLADE_USE_FLAT_CLOUDS` to `NGGL4ESRenderer.kt`:
```kotlin
override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
    buildMap {
        put("LIBGL_GL", "31")
        put("LIBGL_ES", "3")
        put("LIBGL_NORMALIZE", "1")
        put("LIBGL_NOERROR", "1")
        put("LIBGL_NOINTOVLHACK", "1")
        put("LIBGL_MIPMAP", "3")
        put("BLADE_USE_FLAT_CLOUDS", "1")
        if (DeviceHardwareProfiler.getGpuFamily() == GpuFamily.POWERVR) {
            put("LIBGL_SHRINK", "1")
        }
    }
}
```

- [ ] **Step 4: Commit Task 1**
```bash
git add app/BladeLauncher/src/main/java/com/sednium/bladelauncher/utils/device/DeviceHardwareProfiler.kt app/BladeLauncher/src/main/java/com/sednium/bladelauncher/setting/AllSettings.kt app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/renderer/Renderers.kt app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/renderer/renderers/NGGL4ESRenderer.kt
git commit -m "feat: implement DeviceHardwareProfiler with auto driver/renderer resolution"
```

---

### Task 2: Game Graphics Auto-Tuning (`options.txt`, `skyboxify.json`)

**Files:**
- Create: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/tune/GameGraphicsAutoTuner.kt`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/launch/GameLauncher.kt:130`

- [ ] **Step 1: Implement `GameGraphicsAutoTuner.kt`**
Write a tuner that ensures `ao:true`, `cutoutLeaves:true`, `mipmapLevels:4`, `graphicsPreset:"fancy"`, `renderClouds:"true"`, and `"renderStars": true`:
```kotlin
package com.sednium.bladelauncher.game.tune

import com.sednium.bladelauncher.utils.device.DeviceHardwareProfiler
import com.sednium.bladelauncher.utils.logging.Logger
import java.io.File

object GameGraphicsAutoTuner {
    private const val TAG = "GraphicsAutoTuner"

    fun tuneSettings(gameDir: File) {
        runCatching {
            val optionsFile = File(gameDir, "options.txt")
            if (optionsFile.exists()) {
                var content = optionsFile.readText()
                var modified = false

                if (content.contains("ao:false")) {
                    content = content.replace("ao:false", "ao:true")
                    modified = true
                }
                if (content.contains("cutoutLeaves:false")) {
                    content = content.replace("cutoutLeaves:false", "cutoutLeaves:true")
                    modified = true
                }
                if (content.contains("mipmapLevels:0")) {
                    content = content.replace("mipmapLevels:0", "mipmapLevels:4")
                    modified = true
                }
                if (content.contains("graphicsPreset:\"custom\"") || content.contains("graphicsPreset:\"fast\"")) {
                    content = content.replace("graphicsPreset:\"custom\"", "graphicsPreset:\"fancy\"")
                        .replace("graphicsPreset:\"fast\"", "graphicsPreset:\"fancy\"")
                    modified = true
                }
                if (content.contains("renderClouds:\"false\"")) {
                    content = content.replace("renderClouds:\"false\"", "renderClouds:\"true\"")
                    modified = true
                }

                if (modified) {
                    optionsFile.writeText(content)
                    Logger.info(TAG, "Auto-tuned options.txt for optimal block and sky graphics")
                }
            }

            val skyboxifyFile = File(gameDir, "config/skyboxify.json")
            if (skyboxifyFile.exists()) {
                var content = skyboxifyFile.readText()
                if (content.contains("\"renderStars\": false")) {
                    content = content.replace("\"renderStars\": false", "\"renderStars\": true")
                    skyboxifyFile.writeText(content)
                    Logger.info(TAG, "Enabled stars in skyboxify.json")
                }
            }
        }.onFailure {
            Logger.warning(TAG, "Auto-tuning skipped: ${it.message}")
        }
    }
}
```

- [ ] **Step 2: Hook Auto-Tuner in `GameLauncher.kt`**
In `GameLauncher.launch()`, call `GameGraphicsAutoTuner.tuneSettings(version.getGameDir())` before process start.

- [ ] **Step 3: Commit Task 2**
```bash
git add app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/tune/GameGraphicsAutoTuner.kt app/BladeLauncher/src/main/java/com/sednium/bladelauncher/game/launch/GameLauncher.kt
git commit -m "feat: auto-tune options.txt and skyboxify.json for fancy leaves, smooth lighting and stars"
```

---

### Task 3: `MioLibPatcher` Flat Clouds Patch Gated Behind `BLADE_USE_FLAT_CLOUDS`

**Files:**
- Modify: `ShaderFixTransformer.java` (in `MioLibPatcher.jar`)
- Build: Updated `MioLibPatcher.jar`

- [ ] **Step 1: Update `ShaderFixTransformer.java`**
Replace unconditional `CloudRenderer.render` return with runtime check:
```java
        boolean useFlatClouds = "1".equals(System.getenv("BLADE_USE_FLAT_CLOUDS"));

        if (name.equals("net.minecraft.client.renderer.RenderPipelines")) {
            if (useFlatClouds) {
                // Keep static pipelines intact or ensure FLAT_CLOUDS is loaded
                LogUtil.info("ShaderFixTransformer: Retaining pipelines for flat cloud fallback");
            }
        } else if (name.equals("net.minecraft.client.renderer.CloudRenderer")) {
            if (useFlatClouds) {
                for (CtMethod method : ctClass.getDeclaredMethods("render")) {
                    if (method.getParameterTypes().length == 2) {
                        method.insertBefore("{\n" +
                            "    if ($1 == net.minecraft.client.CloudStatus.FANCY) {\n" +
                            "        $1 = net.minecraft.client.CloudStatus.FAST;\n" +
                            "    }\n" +
                            "}");
                        LogUtil.info("ShaderFixTransformer: Routed CloudRenderer to FAST flat clouds under GL4ES");
                    }
                }
            } else {
                LogUtil.info("ShaderFixTransformer: Running native volumetric clouds on desktop Vulkan Zink");
            }
        }
```

- [ ] **Step 2: Recompile and repackage `MioLibPatcher.jar`**
Compile `ShaderFixTransformer.java` using javac with javassist classpath and update `app/BladeLauncher/src/main/assets/components/launcher/MioLibPatcher.jar`.

- [ ] **Step 3: Commit Task 3**
```bash
git add app/BladeLauncher/src/main/assets/components/launcher/MioLibPatcher.jar
git commit -m "fix(shader): gate flat cloud bytecode patch behind BLADE_USE_FLAT_CLOUDS"
```

---

### Task 4: Clean Build, Install & In-Game Verification

**Files:**
- Output: `app/BladeLauncher/build/outputs/apk/release/BladeLauncher-2.4.7-x86_64.apk`

- [ ] **Step 1: Clean build APK with bwrap**
```bash
bwrap --dev-bind / / --bind "$PWD" /tmp/BladeProject bash -c "cd /tmp/BladeProject/app && ./gradlew --no-daemon :BladeLauncher:assembleRelease -Parch=x86_64"
```

- [ ] **Step 2: Install on `emulator-5554` and launch**
Install APK, launch game, load singleplayer world.

- [ ] **Step 3: Capture in-game screenshots and verify**
Verify smooth lighting on blocks, transparent foliage on trees, and sky/clouds rendering in landscape.
