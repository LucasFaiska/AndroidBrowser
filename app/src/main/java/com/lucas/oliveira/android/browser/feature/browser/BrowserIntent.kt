package com.lucas.oliveira.android.browser.feature.browser

sealed interface BrowserIntent {
    data class SubmitUrl(val input: String) : BrowserIntent
    data class UpdateAddressBar(val text: String) : BrowserIntent
}
