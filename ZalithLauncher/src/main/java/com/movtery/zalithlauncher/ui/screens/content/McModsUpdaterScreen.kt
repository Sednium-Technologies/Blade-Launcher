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

package com.movtery.zalithlauncher.ui.screens.content

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun McModsUpdaterScreen(
    key: NormalNavKey.McModsUpdater,
    backStackViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) {
        var currentUrl by rememberSaveable { mutableStateOf(key.initialUrl) }
        var pageTitle by remember { mutableStateOf("MC Mods Updater") }
        var isWebLoading by rememberSaveable { mutableStateOf(true) }
        var canGoBackState by remember { mutableStateOf(false) }
        var canGoForwardState by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val webViewHolder = remember { mutableStateOf<WebView?>(null) }

        BackHandler(enabled = canGoBackState) {
            webViewHolder.value?.let { wv ->
                if (wv.canGoBack()) {
                    wv.goBack()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            // Modern Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                backStackViewModel.mainScreen.popBackStack()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = pageTitle,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            Text(
                                text = currentUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        // Navigation Actions
                        IconButton(
                            enabled = canGoBackState,
                            onClick = { webViewHolder.value?.goBack() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_left_rounded),
                                contentDescription = "Back"
                            )
                        }

                        IconButton(
                            enabled = canGoForwardState,
                            onClick = { webViewHolder.value?.goForward() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                                contentDescription = "Forward"
                            )
                        }

                        IconButton(
                            onClick = { webViewHolder.value?.reload() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_refresh),
                                contentDescription = stringResource(R.string.mc_mods_updater_refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(currentUrl))
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_public),
                                contentDescription = stringResource(R.string.mc_mods_updater_open_browser),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Source Navigation Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickLinks = listOf(
                            "Modrinth Mods" to "https://modrinth.com/mods",
                            "CurseForge" to "https://www.curseforge.com/minecraft/search?class=mc-mods",
                            "Modpacks" to "https://modrinth.com/modpacks",
                            "Resource Packs" to "https://modrinth.com/resourcepacks",
                            "Shaders" to "https://modrinth.com/shaders"
                        )

                        quickLinks.forEach { (name, targetUrl) ->
                            val isSelected = currentUrl.startsWith(targetUrl)
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = {
                                    currentUrl = targetUrl
                                    webViewHolder.value?.loadUrl(targetUrl)
                                },
                                label = {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isWebLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Embedded Browser View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    url?.let { currentUrl = it }
                                    isWebLoading = true
                                    canGoBackState = view?.canGoBack() == true
                                    canGoForwardState = view?.canGoForward() == true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    url?.let { currentUrl = it }
                                    pageTitle = view?.title ?: "MC Mods Updater"
                                    isWebLoading = false
                                    canGoBackState = view?.canGoBack() == true
                                    canGoForwardState = view?.canGoForward() == true
                                }
                            }
                            loadUrl(currentUrl)
                            webViewHolder.value = this
                        }
                    },
                    update = {
                        // Managed by webViewHolder
                    }
                )
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                webViewHolder.value?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    clearHistory()
                    removeAllViews()
                    destroy()
                }
                webViewHolder.value = null
            }
        }
    }
}
