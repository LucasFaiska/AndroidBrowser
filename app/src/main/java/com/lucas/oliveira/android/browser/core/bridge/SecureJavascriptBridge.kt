package com.lucas.oliveira.android.browser.core.bridge

import android.webkit.JavascriptInterface

class SecureJavascriptBridge(
    private val controller: SecureBridgeController,
    private val currentUrlProvider: () -> String?
) {
    @JavascriptInterface
    fun requestDeviceInfo(callbackName: String) {
        val currentUrl = currentUrlProvider()
        controller.handleInteraction(currentUrl, callbackName)
    }
}
