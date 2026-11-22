/*
 * Blade Launcher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.sednium.bladelauncher.game.renderer.renderers

import com.sednium.bladelauncher.game.renderer.RendererInterface

object AutoRenderer : RendererInterface {
    override fun getRendererId(): String = "auto"

    override fun getUniqueIdentifier(): String = "auto"

    override fun getRendererName(): String = "Auto (Recommended)"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { emptyMap() }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = ""
}
