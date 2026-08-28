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

package com.sednium.bladelauncher.ui.base

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sednium.bladelauncher.setting.AllSettings
import com.sednium.bladelauncher.setting.enums.DarkMode

abstract class FullScreenAppCompatActivity : AbstractAppCompatActivity() {
    /**
     * Whether this activity should run in full immersive mode (hiding system bars).
     * By default, launcher and setup screens keep the status bar visible.
     * In-game activities (e.g. VMActivity) override this to true.
     */
    protected open val useFullImmersive: Boolean
        get() = false

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySystemBarsConfig()
    }

    @CallSuper
    override fun onPostResume() {
        super.onPostResume()
        applySystemBarsConfig()
    }

    @CallSuper
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applySystemBarsConfig()
        }
    }

    @Suppress("DEPRECATION")
    private fun applySystemBarsConfig() {
        window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val params = window.attributes
                val newParams = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                if (params.layoutInDisplayCutoutMode != newParams) {
                    params.layoutInDisplayCutoutMode = newParams
                    window.attributes = params
                }
            }

            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            if (useFullImmersive) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
                val isDark = when (AllSettings.launcherDarkMode.getValue()) {
                    DarkMode.Enable -> true
                    DarkMode.Disable -> false
                    DarkMode.FollowSystem -> {
                        val uiMode = resources.configuration.uiMode
                        (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    }
                }
                insetsController.isAppearanceLightStatusBars = !isDark
            }

            window.navigationBarColor = Color.TRANSPARENT
            window.statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }
}

@Composable
fun Modifier.applyFullscreen(value: Boolean): Modifier {
    val modifier = Modifier.fillMaxSize()
    return then(
        if (value) modifier
        else modifier.windowInsetsPadding(WindowInsets.displayCutout)
    )
}