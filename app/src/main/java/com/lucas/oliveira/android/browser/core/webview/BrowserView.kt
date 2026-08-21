package com.lucas.oliveira.android.browser.core.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.lucas.oliveira.android.browser.core.bridge.SecureJavascriptBridge

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserView(
    state: BrowserState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }

                    webViewClient = BrowserWebViewClient(state, context.applicationContext)

                    if (state.secureBridgeController != null) {
                        addJavascriptInterface(
                            SecureJavascriptBridge(state.secureBridgeController!!) { state.url },
                            "AndroidBridgeNative"
                        )
                    }

                    state.webView = this

                    state.savedStateBundle?.let { bundle ->
                        restoreState(bundle)
                        state.canGoBack = canGoBack()
                        state.canGoForward = canGoForward()
                        state.savedStateBundle = null
                    }
                }
            },
            onRelease = { webView ->
                state.webView = null
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize()
        )

        state.error?.let { error ->
            BrowserErrorPage(
                error = error,
                onRetry = { state.reload() }
            )
        }
    }
}
