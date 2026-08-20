package com.lucas.oliveira.android.browser.core.webview

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.lucas.oliveira.android.browser.R

class BrowserWebViewClient(
    private val state: BrowserState,
    private val context: Context
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        state.isLoading = true
        state.url = url
        state.error = null
        state.canGoBack = view?.canGoBack() ?: false
        state.canGoForward = view?.canGoForward() ?: false
    }

    override fun doUpdateVisitedHistory(
        view: WebView?, url: String?, isReload: Boolean
    ) {
        super.doUpdateVisitedHistory(view, url, isReload)
        url?.let { state.url = it }
        state.canGoBack = view?.canGoBack() ?: false
        state.canGoForward = view?.canGoForward() ?: false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        state.isLoading = false
        state.canGoBack = view?.canGoBack() ?: false
        state.canGoForward = view?.canGoForward() ?: false
    }

    override fun onReceivedError(
        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            state.error = BrowserError(
                errorCode = error?.errorCode ?: -1,
                description = error?.description?.toString()
                    ?: context.getString(R.string.error_unknown),
                failingUrl = request.url?.toString() ?: ""
            )
        }
    }
}