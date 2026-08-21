package com.lucas.oliveira.android.browser.core.webview

import android.os.Bundle
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.lucas.oliveira.android.browser.core.bridge.SecureBridgeController

class BrowserState(
    initialUrl: String? = null,
) {
    var url by mutableStateOf(initialUrl)
        internal set

    var isLoading by mutableStateOf(false)
        internal set

    var isRendering by mutableStateOf(false)
        internal set

    var canGoBack by mutableStateOf(false)
        internal set

    var canGoForward by mutableStateOf(false)
        internal set

    var error by mutableStateOf<BrowserError?>(null)
        internal set

    var secureBridgeController: SecureBridgeController? = null

    internal var savedStateBundle: Bundle? = null

    internal var webView: WebView? = null

    fun loadUrl(url: String) {
        this.isRendering = true
        this.error = null
        this.url = url
        webView?.loadUrl(url)
    }

    fun goBack() {
        if (webView?.canGoBack() == true) {
            this.isRendering = true
            webView?.goBack()
        }
    }

    fun goForward() {
        if (webView?.canGoForward() == true) {
            this.isRendering = true
            webView?.goForward()
        }
    }

    fun reload() {
        this.isRendering = true
        this.error = null
        webView?.reload()
    }

    fun clearError() {
        this.error = null
    }

    companion object {
        val Saver: Saver<BrowserState, Bundle> = Saver(
            save = { state ->
                Bundle().apply {
                    putString("current_url", state.url)
                    putBoolean("can_go_back", state.canGoBack)
                    putBoolean("can_go_forward", state.canGoForward)
                    state.webView?.saveState(this)
                }
            },
            restore = { bundle ->
                BrowserState().apply {
                    url = bundle.getString("current_url")
                    canGoBack = bundle.getBoolean("can_go_back", false)
                    canGoForward = bundle.getBoolean("can_go_forward", false)
                    savedStateBundle = bundle
                }
            }
        )
    }
}

data class BrowserError(
    val errorCode: Int,
    val description: String,
    val failingUrl: String
)

@Composable
fun rememberBrowserState(initialUrl: String? = null): BrowserState {
    return rememberSaveable(saver = BrowserState.Saver) {
        BrowserState(initialUrl)
    }
}