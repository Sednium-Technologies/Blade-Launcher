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

package com.sednium.bladelauncher.upgrade

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GitHub Releases API model for querying the latest release of Blade Launcher.
 */
@Serializable
data class GithubReleaseApi(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val body: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null,
    @SerialName("assets")
    val assets: List<Asset> = emptyList()
) {
    @Serializable
    data class Asset(
        @SerialName("name")
        val name: String,
        @SerialName("size")
        val size: Long = 0L,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
        @SerialName("content_type")
        val contentType: String? = null
    )

    fun toRemoteData(): RemoteData {
        val version = tagName.removePrefix("v").trim()
        val rawCode = Regex("""(?:version_code|code)[:=]\s*(\d+)""", RegexOption.IGNORE_CASE)
            .find(body ?: "")?.groupValues?.get(1)?.toIntOrNull()
            ?: version.filter { it.isDigit() }.toIntOrNull()
            ?: 0

        val files = assets.map { asset ->
            val arch = when {
                asset.name.contains("arm64", ignoreCase = true) -> RemoteData.RemoteFile.Arch.ARM64
                asset.name.contains("armeabi", ignoreCase = true) || asset.name.contains("arm", ignoreCase = true) -> RemoteData.RemoteFile.Arch.ARM
                asset.name.contains("x86_64", ignoreCase = true) -> RemoteData.RemoteFile.Arch.X86_64
                asset.name.contains("x86", ignoreCase = true) -> RemoteData.RemoteFile.Arch.X86
                else -> RemoteData.RemoteFile.Arch.ALL
            }
            RemoteData.RemoteFile(
                fileName = asset.name,
                uri = asset.browserDownloadUrl,
                arch = arch,
                size = asset.size
            )
        }

        val defaultBody = RemoteData.RemoteBody(
            language = "en",
            markdown = body ?: name ?: "Blade Launcher $version"
        )

        return RemoteData(
            code = rawCode,
            version = version,
            createdAt = publishedAt ?: createdAt ?: "",
            defaultCloudDrive = null,
            cloudDrives = emptyList(),
            files = files,
            defaultBody = defaultBody,
            bodies = listOf(defaultBody)
        )
    }
}
