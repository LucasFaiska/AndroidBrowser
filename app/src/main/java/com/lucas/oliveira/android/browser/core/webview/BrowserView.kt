package com.lucas.oliveira.android.browser.core.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.lucas.oliveira.android.browser.R

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
                state.pendingAction?.let { action ->
                    when (action) {
                        is BrowserAction.LoadUrl -> webView.loadUrl(action.url)
                        is BrowserAction.GoBack -> webView.goBack()
                        is BrowserAction.GoForward -> webView.goForward()
                        is BrowserAction.Reload -> webView.reload()
                    }
                    state.consumeAction()
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
