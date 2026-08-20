package com.lucas.oliveira.android.browser.feature.browser

sealed interface BrowserSideEffect {
    data class LoadUrl(val url: String) : BrowserSideEffect
}