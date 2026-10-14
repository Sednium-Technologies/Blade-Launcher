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

package com.sednium.bladelauncher.ui.control.input

import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sednium.bladelauncher.R

@Composable
fun HidableInputLayout(
    onSend: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    onClose: () -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    inputFocus: FocusRequester = remember { FocusRequester() },
) {
    var view by remember { mutableStateOf<TouchCharInput?>(null) }

    AndroidView(
        modifier = Modifier
            .alpha(0f)
            .size(1.dp)
            .focusRequester(inputFocus),
        factory = { context ->
            TouchCharInput(context).apply {
                id = View.generateViewId()

                imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or
                        EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                        EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                        EditorInfo.IME_ACTION_DONE
                inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                        InputType.TYPE_TEXT_VARIATION_FILTER or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE

                setEms(10)

                isFocusableInTouchMode = true
                nextFocusDownId = id
                nextFocusUpId = id
                nextFocusLeftId = id
                nextFocusRightId = id
            }.also { view0 ->
                view = view0.also {
                    it.setListener(
                        object : InputListener {
                            override fun onSend(char: Char) {
                                onSend(char.toString())
                            }
                            override fun onBackspace() {
                                onBackspace()
                            }
                            override fun onEnter() {
                                onEnter()
                                onClose()
                            }
                        }
                    )
                }
            }
        }
    )

    LaunchedEffect(view) {
        if (view == null) return@LaunchedEffect
        inputFocus.requestFocus()
        keyboardController?.show()
        view?.enableKeyboard()
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
            view = null
        }
    }

    OnKeyboardClosed {
        keyboardController?.hide()
        onClose()
    }
}

@Composable
fun InGameChatBar(
    onSendText: (String) -> Unit,
    onSendEnter: () -> Unit,
    onSendTab: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    OnKeyboardClosed {
        onClose()
    }

    val handleSend = {
        val text = textFieldValue.text
        if (text.isNotEmpty()) {
            onSendText(text)
            onSendEnter()
            textFieldValue = TextFieldValue("")
            onClose()
        } else {
            onSendEnter()
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quick "/" command button
                FilledTonalIconButton(
                    onClick = {
                        val currentText = textFieldValue.text
                        val (newText, newCursor) = if (currentText.startsWith("/")) {
                            val updated = currentText.removePrefix("/")
                            val cursor = (textFieldValue.selection.end - 1).coerceIn(0, updated.length)
                            Pair(updated, cursor)
                        } else {
                            val updated = "/$currentText"
                            val cursor = (textFieldValue.selection.end + 1).coerceIn(0, updated.length)
                            Pair(updated, cursor)
                        }
                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newCursor)
                        )
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tab autocomplete button
                FilledTonalIconButton(
                    onClick = {
                        onSendTab()
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.size(width = 44.dp, height = 36.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Tab",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Text input field
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { handleSend() },
                            onDone = { handleSend() }
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (textFieldValue.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.game_chat_placeholder),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Send button
                IconButton(
                    onClick = handleSend,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = stringResource(R.string.game_chat_send),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Close button
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onClose()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.generic_close),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}