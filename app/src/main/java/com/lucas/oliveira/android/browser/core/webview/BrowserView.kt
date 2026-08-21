package com.lucas.oliveira.android.browser.core.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.lucas.oliveira.android.browser.core.bridge.SecureJavascriptBridge

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserView(
    state: BrowserState,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.background

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(backgroundColor.toArgb())
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
                update = { webView ->
                    webView.setBackgroundColor(backgroundColor.toArgb())
                },
                onRelease = { webView ->
                    state.webView = null
                    webView.destroy()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (state.isRendering && state.error == null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            state.error?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserErrorPage(
                        error = error,
                        onRetry = { state.reload() }
                    )
                }
            }
        }
    }
}
