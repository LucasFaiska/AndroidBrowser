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
        state.error = null
        state.url = url
        state.isLoading = true
        super.onPageStarted(view, url, favicon)
        state.canGoBack = view?.canGoBack() ?: false
        state.canGoForward = view?.canGoForward() ?: false
    }

    override fun doUpdateVisitedHistory(
        view: WebView?, url: String?, isReload: Boolean
    ) {
        super.doUpdateVisitedHistory(view, url, isReload)
        if (url != null) {
            state.url = url
        }
        state.canGoBack = view?.canGoBack() ?: false
        state.canGoForward = view?.canGoForward() ?: false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        state.isLoading = false
        state.canGoBack = view?.canGoBack() ?: false
        state.canGoForward = view?.canGoForward() ?: false
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        state.isRendering = false
    }

    override fun onReceivedError(
        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val errorCode = error?.errorCode ?: -1
            val description = error?.description?.toString() ?: ""
            if (errorCode == -3 ||
                description == "net::ERR_ABORTED" ||
                description == "ERR_ABORTED" ||
                description.contains("ERR_ABORTED", ignoreCase = true)
            ) {
                return
            }
            state.isRendering = false
            state.error = BrowserError(
                errorCode = errorCode,
                description = description.ifEmpty { context.getString(R.string.error_unknown) },
                failingUrl = request.url?.toString() ?: ""
            )
        }
    }
}
