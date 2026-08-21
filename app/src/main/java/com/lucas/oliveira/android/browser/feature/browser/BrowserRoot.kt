package com.lucas.oliveira.android.browser.feature.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.lucas.oliveira.android.browser.core.webview.rememberBrowserState

@Composable
fun BrowserRoot(
    modifier: Modifier = Modifier,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val browserState = rememberBrowserState()
    browserState.secureBridgeController = viewModel.secureBridgeController

    BrowserScreen(
        uiState = uiState,
        effects = viewModel.effect,
        browserState = browserState,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}
