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

package com.sednium.bladelauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.sednium.bladelauncher.R

val urbanistFontFamily = FontFamily(
    Font(R.font.urbanist_regular, FontWeight.Normal),
    Font(R.font.urbanist_medium, FontWeight.Medium),
    Font(R.font.urbanist_semibold, FontWeight.SemiBold),
    Font(R.font.urbanist_bold, FontWeight.Bold)
)

private val defaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = urbanistFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = urbanistFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = urbanistFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = urbanistFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = urbanistFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = urbanistFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = urbanistFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = urbanistFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = urbanistFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = urbanistFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = urbanistFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = urbanistFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = urbanistFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = urbanistFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = urbanistFontFamily)
)
