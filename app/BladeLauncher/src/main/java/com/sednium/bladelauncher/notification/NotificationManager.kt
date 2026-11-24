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

package com.sednium.bladelauncher.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationManager {
    /**
     * 初始化通知，初始化通知渠道（频道）
     */
    fun initManager(activity: Activity) {
        BladeNotificationManager.initManager(activity)
    }

    /**
     * 尝试检查通知权限是否开启，安卓 13 以下可能没法 100% 确定
     */
    fun checkNotificationEnabled(context: Context): Boolean {
        return BladeNotificationManager.checkNotificationEnabled(context)
    }

    private fun createNotificationChannel(activity: Activity, channelData: NotificationChannelData) {
        BladeNotificationManager.initManager(activity)
    }

    /**
     * 跳转到通知设置页，出现异常则仅跳转到详细设置页
     */
    fun openNotificationSettings(context: Context) {
        BladeNotificationManager.openNotificationSettings(context)
    }
}