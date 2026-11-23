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
                } else if (content.contains("renderClouds:false")) {
                    content = content.replace("renderClouds:false", "renderClouds:true")
                    modified = true
                }

                // If renderDistance is not specified, auto-tune based on hardware profiler.
                // If it is already present, preserve the user's manual choice.
                if (!content.contains("renderDistance:")) {
                    val optimalChunk = DeviceHardwareProfiler.getOptimalChunkDistance()
                    val renderDistanceLine = "renderDistance:$optimalChunk"
                    content = if (content.isEmpty()) {
                        renderDistanceLine + "\n"
                    } else if (content.endsWith("\n")) {
                        content + renderDistanceLine + "\n"
                    } else {
                        content + "\n" + renderDistanceLine + "\n"
                    }
                    modified = true
                    Logger.info(TAG, "Auto-tuned renderDistance: $optimalChunk for GPU: ${DeviceHardwareProfiler.getGpuFamily()}")
                } else {
                    Logger.info(TAG, "Preserving existing user renderDistance setting in options.txt")
                }

                if (modified) {
                    optionsFile.writeText(content)
                    Logger.info(TAG, "Auto-tuned options.txt for optimal block and sky graphics")
                }
            } else {
                // Fresh install: initialize options.txt with optimal chunk distance
                val optimalChunk = DeviceHardwareProfiler.getOptimalChunkDistance()
                optionsFile.parentFile?.mkdirs()
                optionsFile.writeText("renderDistance:$optimalChunk\n")
                Logger.info(TAG, "Created options.txt with initial optimal renderDistance")
            }

            val skyboxifyFile = File(gameDir, "config/skyboxify.json")
            if (skyboxifyFile.exists()) {
                var content = skyboxifyFile.readText()
                if (content.contains("\"renderStars\": false") || content.contains("\"renderStars\":false")) {
                    content = content.replace("\"renderStars\": false", "\"renderStars\": true")
                        .replace("\"renderStars\":false", "\"renderStars\": true")
                    skyboxifyFile.writeText(content)
                    Logger.info(TAG, "Enabled stars in skyboxify.json")
                }
            }
        }.onFailure {
            Logger.warning(TAG, "Auto-tuning skipped: ${it.message}")
        }
    }
}
