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

package com.sednium.bladelauncher.game.launch

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sednium.bladelauncher.R
import com.sednium.bladelauncher.notification.BladeNotificationManager

class GameService : Service() {
    companion object {
        const val ACTION_TERMINATE = "com.sednium.bladelauncher.ACTION_TERMINATE_GAME"
        const val EXTRA_VERSION_NAME = "extra_version_name"
        const val EXTRA_PROFILE_NAME = "extra_profile_name"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_TERMINATE) {
            terminateGameProcess()
            return START_NOT_STICKY
        }

        val versionName = intent?.getStringExtra(EXTRA_VERSION_NAME) ?: "Minecraft"
        val profileName = intent?.getStringExtra(EXTRA_PROFILE_NAME) ?: "Default"

        val terminateIntent = Intent(this, GameService::class.java).apply {
            action = ACTION_TERMINATE
        }
        val terminatePendingIntent = PendingIntent.getService(
            this,
            100,
            terminateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = BladeNotificationManager.CHANNEL_GAME_RUNNING

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_game_running_title))
            .setContentText("$versionName • $profileName")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(
                R.drawable.ic_close,
                getString(R.string.notification_game_terminate),
                terminatePendingIntent
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                BladeNotificationManager.NOTIFICATION_ID_GAME_SERVICE,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(BladeNotificationManager.NOTIFICATION_ID_GAME_SERVICE, notification)
        }

        return START_NOT_STICKY
    }

    private fun terminateGameProcess() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        stopSelf()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
