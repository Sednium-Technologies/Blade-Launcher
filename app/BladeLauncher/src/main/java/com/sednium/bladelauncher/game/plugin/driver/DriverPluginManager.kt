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

package com.sednium.bladelauncher.game.plugin.driver

import android.content.Context
import android.content.pm.ApplicationInfo
import com.sednium.bladelauncher.R
import com.sednium.bladelauncher.game.plugin.ApkPlugin
import com.sednium.bladelauncher.game.plugin.ApkPluginManager
import com.sednium.bladelauncher.game.plugin.cacheAppIcon
import com.sednium.bladelauncher.setting.AllSettings
import com.sednium.bladelauncher.utils.device.DeviceHardwareProfiler

/**
 * FCL 驱动器插件
 * [FCL DriverPlugin.kt](https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCLauncher/src/main/java/com/tungsten/fclauncher/plugins/DriverPlugin.kt)
 */
object DriverPluginManager: ApkPluginManager() {
    private val driverList: MutableList<Driver> = mutableListOf()

    @JvmStatic
    fun getDriverList(): List<Driver> = driverList.toList()

    private lateinit var currentDriver: Driver

    @JvmStatic
    @JvmOverloads
    fun setDriverById(driverId: String, context: Context? = null) {
        if (driverId.isNotEmpty() && driverId != "auto") {
            val userDriver = driverList.find { it.id == driverId || it.name == driverId }
            if (userDriver != null) {
                currentDriver = userDriver
                com.sednium.bladelauncher.utils.logging.Logger.info("DriverPluginManager", "Current driver set to: ${currentDriver.name} (id: ${currentDriver.id}) for requested: $driverId")
                return
            }
        }
        currentDriver = DeviceHardwareProfiler.resolveDriver(context, driverId)
        com.sednium.bladelauncher.utils.logging.Logger.info("DriverPluginManager", "Current driver set to: ${currentDriver.name} (id: ${currentDriver.id}) for requested: $driverId")
    }

    @JvmStatic
    fun getDriver(): Driver = if (::currentDriver.isInitialized) currentDriver else (driverList.find { it.id != "auto" } ?: driverList.firstOrNull() ?: Driver(
        id = "default turnip",
        appName = "",
        appVersion = "",
        name = "Turnip",
        path = "",
        isLauncher = true
    ))

    /**
     * 初始化驱动器
     */
    fun initDriver(context: Context) {
        driverList.clear()
        val applicationInfo = context.applicationInfo
        driverList.add(
            Driver(
                id = "auto",
                appName = "",
                appVersion = "",
                name = "Auto (Recommended)",
                path = applicationInfo.nativeLibraryDir,
                isLauncher = true
            )
        )
        driverList.add(
            Driver(
                id = "default turnip",
                appName = "",
                appVersion = "",
                name = "Turnip",
                path = applicationInfo.nativeLibraryDir,
                isLauncher = true
            )
        )
        setDriverById(AllSettings.vulkanDriver.getValue(), context)
    }

    /**
     * 通用 FCL 插件
     */
    override fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit
    ) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (metaData.getBoolean("fclPlugin", false)) {
                val driver = metaData.getString("driver") ?: return
                val nativeLibraryDir = info.nativeLibraryDir

                val packageManager = context.packageManager
                val packageName = info.packageName
                val appName = info.loadLabel(packageManager).toString()
                val appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: ""

                val plugin = Driver(
                    id = packageName,
                    appName = appName,
                    appVersion = appVersion,
                    name = driver,
                    summary = context.getString(R.string.settings_renderer_from_plugins, appName),
                    path = nativeLibraryDir,
                    isLauncher = false
                )

                driverList.add(plugin)

                runCatching {
                    cacheAppIcon(context, info)
                    loaded(plugin)
                }
            }
        }
    }
}