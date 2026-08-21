package com.lucas.oliveira.android.browser.feature.browser

import com.lucas.oliveira.android.browser.core.bridge.PermissionDecision

data class BrowserUiState(
    val addressBarText: String = "",
    val activeDialog: BridgeDialogState? = null
)

data class BridgeDialogState(
    val origin: String,
    val onResponse: (PermissionDecision) -> Unit
)
