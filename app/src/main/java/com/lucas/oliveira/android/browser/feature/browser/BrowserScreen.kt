package com.lucas.oliveira.android.browser.feature.browser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lucas.oliveira.android.browser.R
import com.lucas.oliveira.android.browser.core.bridge.PermissionDecision
import com.lucas.oliveira.android.browser.core.webview.BrowserState
import com.lucas.oliveira.android.browser.core.webview.BrowserView
import kotlinx.coroutines.flow.Flow

@Composable
fun BrowserScreen(
    uiState: BrowserUiState,
    effects: Flow<BrowserSideEffect>,
    browserState: BrowserState,
    onIntent: (BrowserIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is BrowserSideEffect.LoadUrl -> browserState.loadUrl(effect.url)
                is BrowserSideEffect.EvaluateJavascript -> {
                    browserState.webView?.evaluateJavascript(effect.script, null)
                }
            }
        }
    }

    LaunchedEffect(browserState.url) {
        browserState.url?.let { newUrl ->
            if (newUrl.isNotBlank() && newUrl != uiState.addressBarText) {
                onIntent(BrowserIntent.UpdateAddressBar(newUrl))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    AddressBar(
                        text = uiState.addressBarText,
                        onTextChange = { onIntent(BrowserIntent.UpdateAddressBar(it)) },
                        onSubmit = { onIntent(BrowserIntent.SubmitUrl(uiState.addressBarText)) },
                        onBack = { browserState.goBack() },
                        onForward = { browserState.goForward() },
                        onReload = { browserState.reload() },
                        canGoBack = browserState.canGoBack,
                        canGoForward = browserState.canGoForward
                    )

                    if (browserState.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        BrowserView(
            state = browserState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Aplica estritamente a altura exata da topBar
        )
    }

    uiState.activeDialog?.let { dialog ->
        PermissionDialog(dialog)
    }
}

@Composable
private fun PermissionDialog(
    dialog: BridgeDialogState,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = {
            dialog.onResponse(PermissionDecision.DENIED)
        },
        title = {
            Text(stringResource(R.string.bridge_dialog_title))
        },
        text = {
            Text(stringResource(R.string.bridge_dialog_message, dialog.origin))
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { dialog.onResponse(PermissionDecision.ALLOWED_ALWAYS) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.bridge_dialog_allow_always))
                }
                OutlinedButton(
                    onClick = { dialog.onResponse(PermissionDecision.ALLOWED_ONCE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.bridge_dialog_allow_once))
                }
                TextButton(
                    onClick = { dialog.onResponse(PermissionDecision.DENIED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.bridge_dialog_deny))
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun AddressBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, enabled = canGoBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_description_back)
            )
        }
        IconButton(onClick = onForward, enabled = canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.content_description_forward)
            )
        }
        IconButton(onClick = onReload) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.content_description_reload)
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text(stringResource(R.string.address_bar_placeholder)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onSubmit() })
        )
    }
}
