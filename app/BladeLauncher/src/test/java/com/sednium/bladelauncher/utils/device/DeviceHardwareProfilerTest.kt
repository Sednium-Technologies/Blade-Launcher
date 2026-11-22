package com.sednium.bladelauncher.utils.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DeviceHardwareProfilerTest {

    @Test
    fun testParseGpuFamilyAdreno() {
        assertEquals(
            GpuFamily.QUALCOMM_ADRENO,
            DeviceHardwareProfiler.parseGpuFamily("Qualcomm", "Adreno (TM) 730")
        )
        assertEquals(
            GpuFamily.QUALCOMM_ADRENO,
            DeviceHardwareProfiler.parseGpuFamily("qualcomm", "adreno (tm) 660")
        )
        assertEquals(
            GpuFamily.QUALCOMM_ADRENO,
            DeviceHardwareProfiler.parseGpuFamily("Qualcomm Technologies, Inc", "Adreno 640")
        )
    }

    @Test
    fun testParseGpuFamilyMali() {
        assertEquals(
            GpuFamily.ARM_MALI,
            DeviceHardwareProfiler.parseGpuFamily("ARM", "Mali-G78")
        )
        assertEquals(
            GpuFamily.ARM_MALI,
            DeviceHardwareProfiler.parseGpuFamily("ARM", "Mali-G710 MC10")
        )
        assertEquals(
            GpuFamily.ARM_MALI,
            DeviceHardwareProfiler.parseGpuFamily("ARM", "Immortalis-G720 MC12")
        )
    }

    @Test
    fun testParseGpuFamilyPowerVR() {
        assertEquals(
            GpuFamily.POWERVR,
            DeviceHardwareProfiler.parseGpuFamily("Imagination Technologies", "PowerVR Rogue GE8320")
        )
        assertEquals(
            GpuFamily.POWERVR,
            DeviceHardwareProfiler.parseGpuFamily("IMG", "B-Series BXM-4-64")
        )
        assertEquals(
            GpuFamily.POWERVR,
            DeviceHardwareProfiler.parseGpuFamily("Imagination", "Rogue G6430")
        )
    }

    @Test
    fun testParseGpuFamilyIntelMesa() {
        assertEquals(
            GpuFamily.INTEL_MESA,
            DeviceHardwareProfiler.parseGpuFamily("Intel Open Source Technology Center", "Mesa Intel(R) UHD Graphics 620")
        )
        assertEquals(
            GpuFamily.INTEL_MESA,
            DeviceHardwareProfiler.parseGpuFamily("Mesa/X.org", "llvmpipe (LLVM 15.0.7, 256 bits)")
        )
        assertEquals(
            GpuFamily.INTEL_MESA,
            DeviceHardwareProfiler.parseGpuFamily("Google (NVIDIA Corporation)", "Android Emulator OpenGL ES Translator (ranchu)")
        )
    }

    @Test
    fun testParseGpuFamilyUnknown() {
        assertEquals(
            GpuFamily.UNKNOWN,
            DeviceHardwareProfiler.parseGpuFamily("Custom Vendor", "Custom GPU 1000")
        )
        assertEquals(
            GpuFamily.UNKNOWN,
            DeviceHardwareProfiler.parseGpuFamily("", "")
        )
    }

    @Test
    fun testOptimalChunkDistance() {
        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.POWERVR)
        assertEquals(4, DeviceHardwareProfiler.getOptimalChunkDistance())

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.ARM_MALI)
        assertEquals(6, DeviceHardwareProfiler.getOptimalChunkDistance())

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.QUALCOMM_ADRENO)
        assertEquals(10, DeviceHardwareProfiler.getOptimalChunkDistance())

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.INTEL_MESA)
        assertEquals(8, DeviceHardwareProfiler.getOptimalChunkDistance())

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.UNKNOWN)
        assertEquals(6, DeviceHardwareProfiler.getOptimalChunkDistance())

        // Reset
        DeviceHardwareProfiler.setGpuForTesting(null)
    }

    @Test
    fun testNggl4esRendererEnvFlatClouds() {
        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.QUALCOMM_ADRENO)
        val envAdreno = com.sednium.bladelauncher.game.renderer.renderers.NGGL4ESRenderer.getRendererEnv().value
        assertEquals("1", envAdreno["BLADE_USE_FLAT_CLOUDS"])
        assertEquals(null, envAdreno["LIBGL_SHRINK"])

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.POWERVR)
        val envPowerVR = com.sednium.bladelauncher.game.renderer.renderers.NGGL4ESRenderer.getRendererEnv().value
        assertEquals("1", envPowerVR["BLADE_USE_FLAT_CLOUDS"])
        assertEquals("1", envPowerVR["LIBGL_SHRINK"])

        DeviceHardwareProfiler.setGpuForTesting(null)
    }

    @Test
    fun testResolveDriverAutoAdrenoVsMali() {
        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.QUALCOMM_ADRENO)
        val adrenoDriver = DeviceHardwareProfiler.resolveDriver(null, "auto")
        assertEquals("Turnip", adrenoDriver.name)

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.ARM_MALI)
        val maliDriver = DeviceHardwareProfiler.resolveDriver(null, "auto")
        // If no non-Turnip driver installed, falls back safely to first candidate without crashing
        assertNotNull(maliDriver)

        DeviceHardwareProfiler.setGpuForTesting(GpuFamily.POWERVR)
        val powervrDriver = DeviceHardwareProfiler.resolveDriver(null, "auto")
        assertNotNull(powervrDriver)

        // Manual driver override
        val manualDriver = DeviceHardwareProfiler.resolveDriver(null, "Turnip")
        assertEquals("Turnip", manualDriver.name)

        DeviceHardwareProfiler.setGpuForTesting(null)
    }
}
