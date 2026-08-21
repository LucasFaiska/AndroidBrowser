package com.lucas.oliveira.android.browser.feature.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucas.oliveira.android.browser.core.bridge.BridgePermissionStore
import com.lucas.oliveira.android.browser.core.bridge.PermissionDecision
import com.lucas.oliveira.android.browser.core.bridge.SecureBridgeController
import com.lucas.oliveira.android.browser.core.url.UrlResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val urlResolver: UrlResolver,
    private val permissionStore: BridgePermissionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _effect = Channel<BrowserSideEffect>(Channel.BUFFERED)
    val effect: Flow<BrowserSideEffect> = _effect.receiveAsFlow()

    val secureBridgeController = SecureBridgeController(
        permissionStore = permissionStore,
        onShowDialog = ::showPermissionDialog,
        onResolveInteraction = ::resolveBridgeInteraction
    )

    private fun showPermissionDialog(origin: String, callback: (PermissionDecision) -> Unit) {
        _uiState.update {
            it.copy(
                activeDialog = BridgeDialogState(
                    origin = origin,
                    onResponse = { decision ->
                        _uiState.update { state -> state.copy(activeDialog = null) }
                        callback(decision)
                    }
                )
            )
        }
    }

    private fun resolveBridgeInteraction(callbackName: String, data: String) {
        val script = "window.$callbackName('$data')"
        viewModelScope.launch {
            _effect.send(BrowserSideEffect.EvaluateJavascript(script))
        }
    }

    fun onIntent(intent: BrowserIntent) {
        when (intent) {
            is BrowserIntent.SubmitUrl -> handleSubmitUrl(intent.input)
            is BrowserIntent.UpdateAddressBar -> handleUpdateAddressBar(intent.text)
        }
    }

    private fun handleSubmitUrl(input: String) {
        val targetUrl = urlResolver.resolve(input)
        _uiState.update { it.copy(addressBarText = targetUrl) }

        viewModelScope.launch {
            _effect.send(BrowserSideEffect.LoadUrl(targetUrl))
        }
    }

    private fun handleUpdateAddressBar(text: String) {
        _uiState.update { it.copy(addressBarText = text) }
    }
}
