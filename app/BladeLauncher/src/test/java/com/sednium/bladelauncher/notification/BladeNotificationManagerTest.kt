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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BladeNotificationManagerTest {

    @Test
    fun testChannelIdsConfigured() {
        assertEquals("channel_game_running", BladeNotificationManager.CHANNEL_GAME_RUNNING)
        assertEquals("channel_ota_updates", BladeNotificationManager.CHANNEL_OTA_UPDATES)
        assertEquals("channel_download_progress", BladeNotificationManager.CHANNEL_DOWNLOAD_PROGRESS)
    }

    @Test
    fun testNotificationIdsUnique() {
        val ids = setOf(
            BladeNotificationManager.NOTIFICATION_ID_JVM_SERVICE,
            BladeNotificationManager.NOTIFICATION_ID_GAME_SERVICE,
            BladeNotificationManager.NOTIFICATION_ID_VPN_REQUEST_CODE,
            BladeNotificationManager.NOTIFICATION_ID_OTA_UPDATE,
            BladeNotificationManager.NOTIFICATION_ID_TASK_PROGRESS
        )
        assertEquals(5, ids.size)
    }

    @Test
    fun testExtraKeysDefined() {
        assertNotNull(BladeNotificationManager.EXTRA_OPEN_OTA_MODAL)
    }
}
