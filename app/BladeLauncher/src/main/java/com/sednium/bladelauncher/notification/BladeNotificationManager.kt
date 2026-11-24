/*
 * Blade Launcher
 * Copyright (C) 2026 Sednium Technologies and contributors
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
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sednium.bladelauncher.R
import com.sednium.bladelauncher.ui.activities.MainActivity

@Keep
object BladeNotificationManager {
    const val CHANNEL_GAME_RUNNING = "channel_game_running"
    const val CHANNEL_OTA_UPDATES = "channel_ota_updates"
    const val CHANNEL_DOWNLOAD_PROGRESS = "channel_download_progress"

    const val NOTIFICATION_ID_JVM_SERVICE = 1001
    const val NOTIFICATION_ID_GAME_SERVICE = 1002
    const val NOTIFICATION_ID_VPN_REQUEST_CODE = 1003
    const val NOTIFICATION_ID_OTA_UPDATE = 2001
    const val NOTIFICATION_ID_TASK_PROGRESS = 2002

    const val EXTRA_OPEN_OTA_MODAL = "extra_open_ota_modal"

    /**
     * Initialize all notification channels.
     */
    fun initManager(context: Context) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? AndroidNotificationManager ?: return

        // 1. Game running channel (ongoing, low importance)
        val gameRunningChannel = NotificationChannel(
            CHANNEL_GAME_RUNNING,
            context.getString(R.string.notification_game_running_title),
            AndroidNotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains running Minecraft session and enables quick termination"
            setShowBadge(false)
        }
        manager.createNotificationChannel(gameRunningChannel)

        // 2. OTA updates channel (default importance, with badge)
        val otaUpdatesChannel = NotificationChannel(
            CHANNEL_OTA_UPDATES,
            context.getString(R.string.notification_update_available_title),
            AndroidNotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when a new Blade Launcher release is available"
            setShowBadge(true)
        }
        manager.createNotificationChannel(otaUpdatesChannel)

        // 3. Background download progress channel (low importance, no badge)
        val downloadProgressChannel = NotificationChannel(
            CHANNEL_DOWNLOAD_PROGRESS,
            context.getString(R.string.notification_download_progress_title),
            AndroidNotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows live download and game verification progress in background"
            setShowBadge(false)
        }
        manager.createNotificationChannel(downloadProgressChannel)

        // Legacy channels compatibility
        NotificationChannelData.entries.forEach { data ->
            val legacyChannel = NotificationChannel(
                data.channelId,
                data.channelName(context),
                data.level
            ).apply {
                data.channelDescription?.invoke(context)?.let { desc ->
                    description = desc
                }
                setShowBadge(data.showBadge)
            }
            manager.createNotificationChannel(legacyChannel)
        }
    }

    /**
     * Check if notifications are enabled (Android 13+ runtime permission aware).
     */
    fun checkNotificationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_DENIED
        }
    }

    /**
     * Direct the user to the app notification settings page.
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Post a notification alerting the user that a new APK release is available.
     * Tapping opens MainActivity straight into the OTA update modal.
     */
    fun postOtaUpdateNotification(context: Context, newVersion: String) {
        if (!checkNotificationEnabled(context)) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_OTA_MODAL, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_OTA_UPDATE,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OTA_UPDATES)
            .setContentTitle(context.getString(R.string.notification_update_available_title))
            .setContentText(context.getString(R.string.notification_update_available_content, newVersion))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? AndroidNotificationManager
        manager?.notify(NOTIFICATION_ID_OTA_UPDATE, notification)
    }

    /**
     * Dismiss the OTA update notification.
     */
    fun cancelOtaUpdateNotification(context: Context) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? AndroidNotificationManager
        manager?.cancel(NOTIFICATION_ID_OTA_UPDATE)
    }

    /**
     * Update the background download/verification progress notification.
     */
    fun updateDownloadProgress(
        context: Context,
        taskTitle: String,
        detailText: String,
        progressPercent: Int,
        isIndeterminate: Boolean
    ) {
        if (!checkNotificationEnabled(context)) return

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TASK_PROGRESS,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DOWNLOAD_PROGRESS)
            .setContentTitle(taskTitle.ifBlank { context.getString(R.string.notification_download_progress_title) })
            .setContentText(detailText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isIndeterminate) {
            builder.setProgress(100, 0, true)
        } else {
            builder.setProgress(100, progressPercent.coerceIn(0, 100), false)
        }

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? AndroidNotificationManager
        manager?.notify(NOTIFICATION_ID_TASK_PROGRESS, builder.build())
    }

    /**
     * Cancel the background download progress notification.
     */
    fun cancelDownloadProgress(context: Context) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? AndroidNotificationManager
        manager?.cancel(NOTIFICATION_ID_TASK_PROGRESS)
    }
}
