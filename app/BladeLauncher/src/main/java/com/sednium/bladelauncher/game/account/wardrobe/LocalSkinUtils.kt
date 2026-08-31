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

package com.sednium.bladelauncher.game.account.wardrobe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.alpha
import com.sednium.bladelauncher.utils.image.isColorMatch
import com.sednium.bladelauncher.utils.image.recycleIfLarge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import com.sednium.bladelauncher.game.account.accountUUID
import com.sednium.bladelauncher.game.account.getUUIDFromUserName
import java.util.UUID

private fun getLocalUuid(name: String): String {
    return accountUUID(getUUIDFromUserName(name))
}

/**
 * 根据皮肤模型类型，生成 profileId
 */
fun getLocalUUIDWithSkinModel(userName: String, skinModelType: SkinModelType): String {
    val baseUuid = getLocalUuid(userName)
    if (skinModelType == SkinModelType.NONE) return baseUuid

    val prefix = baseUuid.take(27)
    val a = baseUuid[7].digitToInt(16)
    val b = baseUuid[15].digitToInt(16)
    val c = baseUuid[23].digitToInt(16)

    var suffix = baseUuid.substring(27).toLong(16)
    val maxSuffix = 0xFFFFFL

    repeat(maxSuffix.toInt() + 1) {
        val currentD = (suffix and 0xFL).toInt()
        if ((a xor b xor c xor currentD) % 2 == skinModelType.targetParity) {
            return prefix + suffix.toString(16).padStart(5, '0').uppercase()
        }
        suffix = if (suffix == maxSuffix) 0L else suffix + 1
    }

    return prefix + suffix.toString(16).padStart(5, '0').uppercase()
}

/**
 * 获取图像尺寸 (width, height)
 */
fun getImageDimensions(file: File): Pair<Int, Int> {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.absolutePath, options)
    return Pair(options.outWidth, options.outHeight)
}

/**
 * 检查皮肤像素合法性，Minecraft支持64x64或64x32像素皮肤，以及正整数倍的HD高清皮肤（如128x128, 256x256等）
 */
suspend fun validateSkinFile(skinFile: File): Boolean {
    return withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(skinFile.absolutePath, options)
        options.isValidSkin()
    }
}

/**
 * 检查披风像素合法性，Minecraft披风通常为64x32或64x64，以及其正整数倍的HD高清贴图（如128x64, 256x128等）
 */
suspend fun validateCapeFile(capeFile: File): Boolean {
    return withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(capeFile.absolutePath, options)
        options.isValidCape()
    }
}

/**
 * 是否为有效的皮肤：
 * 1. 双层皮肤：正方形（64x64, 128x128, 256x256, 512x512, 1024x1024等，且为64的倍数）
 * 2. 单层经典皮肤：2:1 比例（64x32, 128x64, 256x128等，且宽度为64的倍数）
 */
fun BitmapFactory.Options.isValidSkin(): Boolean {
    return isDualLayerSkin() || isClassicSkin()
}

/**
 * 是否为双层皮肤：正方形且宽度为64的倍数 (64x64, 128x128, etc.)
 */
fun BitmapFactory.Options.isDualLayerSkin(): Boolean {
    return outWidth > 0 && outHeight > 0 && outWidth == outHeight && (outWidth % 64 == 0)
}

/**
 * 是否为经典单层皮肤：2:1比例且宽度为64的倍数 (64x32, 128x64, etc.)
 */
fun BitmapFactory.Options.isClassicSkin(): Boolean {
    return outWidth > 0 && outHeight > 0 && outWidth == (outHeight * 2) && (outWidth % 64 == 0)
}

/**
 * 是否为有效的披风：
 * 1. 标准/经典披风：2:1 比例（64x32, 128x64, 256x128 等）
 * 2. 正方形披风：1:1 比例（64x64, 128x128 等）
 */
fun BitmapFactory.Options.isValidCape(): Boolean {
    if (outWidth <= 0 || outHeight <= 0) return false
    if (outWidth == (outHeight * 2) && (outWidth % 64 == 0)) return true
    if (outWidth == outHeight && (outWidth % 64 == 0)) return true
    // 同时兼容 64x32 基础尺寸
    return outWidth == 64 && outHeight == 32
}

/**
 * 检查皮肤是否为纤细（Alex）模型，支持高清HD皮肤坐标等比缩放探测
 */
suspend fun File.isSlimModel(): Boolean = withContext(Dispatchers.IO) {
    val options = BitmapFactory.Options()
    val bitmap = BitmapFactory.decodeFile(absolutePath, options) ?: return@withContext false
    try {
        if (options.isClassicSkin()) {
            //旧版单层皮肤不支持细臂
            false
        } else {
            val scale = (options.outWidth / 64).coerceAtLeast(1)
            val rightHand = bitmap.isTransparent((50 * scale) until (52 * scale), (16 * scale) until (20 * scale))
            val rightArm = bitmap.isTransparent((54 * scale) until (56 * scale), (20 * scale) until (32 * scale))

            val leftHand = bitmap.isTransparent((42 * scale) until (44 * scale), (48 * scale) until (52 * scale))
            val leftArm = bitmap.isTransparent((46 * scale) until (48 * scale), (52 * scale) until (64 * scale))

            rightHand && rightArm && leftHand && leftArm
        }
    } catch (_: Exception) {
        false
    } finally {
        bitmap.recycleIfLarge()
    }
}

private fun Bitmap.isTransparent(xRange: IntRange, yRange: IntRange): Boolean {
    return isColorMatch(
        xRange = xRange,
        yRange = yRange,
        predicate = { color, _, _ ->
            color.alpha == 0
        },
        requireAll = true
    )
}
