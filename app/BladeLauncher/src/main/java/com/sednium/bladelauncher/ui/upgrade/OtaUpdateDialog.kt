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

package com.sednium.bladelauncher.ui.upgrade

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sednium.bladelauncher.R
import com.sednium.bladelauncher.ui.components.CheckChip
import com.sednium.bladelauncher.ui.theme.cardColor
import com.sednium.bladelauncher.ui.theme.onCardColor
import com.sednium.bladelauncher.upgrade.RemoteData
import com.sednium.bladelauncher.utils.device.Architecture
import com.sednium.bladelauncher.utils.file.formatFileSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun OtaUpdateDialog(
    data: RemoteData,
    targetFile: RemoteData.RemoteFile?,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFile by remember(data, targetFile) {
        mutableStateOf(targetFile ?: OtaUpdater.resolveTargetApk(data))
    }

    var downloadState by remember { mutableStateOf<OtaDownloadState>(OtaDownloadState.Idle) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }

    fun startDownload(fileToDownload: RemoteData.RemoteFile?) {
        if (fileToDownload == null) {
            downloadState = OtaDownloadState.Error("No compatible APK release found for this device architecture.")
            return
        }
        downloadJob?.cancel()
        downloadState = OtaDownloadState.Idle
        downloadJob = scope.launch {
            OtaUpdater.downloadApk(context, fileToDownload) { state ->
                downloadState = state
            }
        }
    }

    LaunchedEffect(selectedFile) {
        startDownload(selectedFile)
    }

    Dialog(
        onDismissRequest = {
            downloadJob?.cancel()
            onDismissRequest()
        }
    ) {
        Surface(
            modifier = Modifier.padding(all = 4.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with Detected Architecture Showcase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.ic_download),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Blade Launcher v${data.version}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        val primaryAbi = remember {
                            Architecture.getDevicePrimaryAbi().ifBlank {
                                Architecture.archAsStringAndroid(Architecture.getRecommendedArch())
                            }
                        }
                        Text(
                            text = stringResource(R.string.ota_detected_arch, primaryAbi),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Architecture Showcase & Selector (Switch between arm64-v8a, armeabi-v7a, x86_64)
                if (data.files.size > 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.ota_select_arch),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            data.files.forEach { remoteFile ->
                                val isSelected = remoteFile == selectedFile
                                val isRecommended = OtaUpdater.isArchRecommended(remoteFile.arch)
                                CheckChip(
                                    selected = isSelected,
                                    label = {
                                        val label = buildString {
                                            append(OtaUpdater.getArchLabel(remoteFile.arch))
                                            if (isRecommended) {
                                                append(" (${stringResource(R.string.ota_recommended)})")
                                            }
                                        }
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    onClick = {
                                        if (selectedFile != remoteFile) {
                                            selectedFile = remoteFile
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Target File Details
                selectedFile?.let { currentFile ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f, fill = false),
                            text = currentFile.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentFile.size > 0L) {
                            Text(
                                text = formatFileSize(currentFile.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Body State
                when (val state = downloadState) {
                    is OtaDownloadState.Idle -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = stringResource(R.string.ota_downloading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is OtaDownloadState.Downloading -> {
                        val progress = state.progress
                        if (progress >= 0f) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val downloadedStr = formatFileSize(state.bytesDownloaded)
                            val totalStr = if (state.totalBytes > 0L) formatFileSize(state.totalBytes) else "--"
                            Text(
                                text = "$downloadedStr / $totalStr",
                                style = MaterialTheme.typography.labelMedium
                            )

                            val speedStr = if (state.speedBytesPerSec > 0L) "${formatFileSize(state.speedBytesPerSec)}/s" else ""
                            val percentStr = if (progress >= 0f) "${(progress * 100).toInt()}%" else ""
                            Text(
                                text = listOf(speedStr, percentStr).filter { it.isNotBlank() }.joinToString(" • "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is OtaDownloadState.ReadyToInstall -> {
                        Text(
                            text = stringResource(R.string.ota_ready_to_install),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!OtaUpdater.canRequestPackageInstalls(context)) {
                            Text(
                                text = stringResource(R.string.ota_permission_request),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is OtaDownloadState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (val state = downloadState) {
                        is OtaDownloadState.Idle, is OtaDownloadState.Downloading -> {
                            FilledTonalButton(
                                onClick = {
                                    downloadJob?.cancel()
                                    onDismissRequest()
                                }
                            ) {
                                Text(text = stringResource(R.string.ota_cancel))
                            }
                        }
                        is OtaDownloadState.ReadyToInstall -> {
                            FilledTonalButton(
                                onClick = { onDismissRequest() }
                            ) {
                                Text(text = stringResource(R.string.generic_cancel))
                            }
                            Spacer(Modifier.size(8.dp))
                            Button(
                                onClick = {
                                    OtaUpdater.installApk(context, state.apkFile)
                                }
                            ) {
                                Text(text = stringResource(R.string.ota_install))
                            }
                        }
                        is OtaDownloadState.Error -> {
                            FilledTonalButton(
                                onClick = { onDismissRequest() }
                            ) {
                                Text(text = stringResource(R.string.generic_cancel))
                            }
                            Spacer(Modifier.size(8.dp))
                            Button(
                                onClick = { startDownload(selectedFile) }
                            ) {
                                Text(text = stringResource(R.string.generic_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}
