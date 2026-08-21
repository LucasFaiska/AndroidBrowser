package com.lucas.oliveira.android.browser.core.bridge

import java.net.URI

class SecureBridgeController(
    private val permissionStore: BridgePermissionStore,
    private val onShowDialog: (origin: String, callback: (PermissionDecision) -> Unit) -> Unit,
    private val onResolveInteraction: (callbackName: String, data: String) -> Unit
) {
    fun handleInteraction(currentUrl: String?, callbackName: String) {
        val origin = extractOrigin(currentUrl)
        if (origin == null) {
            onResolveInteraction(callbackName, "Error: Invalid Origin")
            return
        }

        val decision = permissionStore.getPermission(origin)
        when (decision) {
            PermissionDecision.ALLOWED_ONCE,
            PermissionDecision.ALLOWED_ALWAYS -> {
                onResolveInteraction(
                    callbackName,
                    """{"appVersion":"1.0.0","osVersion":"Android 14","batteryLevel":85}"""
                )
            }

            PermissionDecision.DENIED -> {
                onResolveInteraction(callbackName, "Error: Permission Denied")
            }

            PermissionDecision.UNDETERMINED -> {
                onShowDialog(origin) { result ->
                    permissionStore.setPermission(origin, result)
                    if (result == PermissionDecision.ALLOWED_ONCE || result == PermissionDecision.ALLOWED_ALWAYS) {
                        onResolveInteraction(
                            callbackName,
                            """{"appVersion":"1.0.0","osVersion":"Android 14","batteryLevel":85}"""
                        )
                    } else {
                        onResolveInteraction(callbackName, "Error: Permission Denied")
                    }
                }
            }
        }
    }

    private fun extractOrigin(currentUrl: String?): String? {
        if (currentUrl == null) return null
        if (currentUrl.isBlank()) return null
        return runCatching {
            val uri = URI(currentUrl)
            val scheme = uri.scheme ?: return null
            val host = uri.host ?: return null
            val port = uri.port
            if (port != -1) {
                "$scheme://$host:$port"
            } else {
                "$scheme://$host"
            }
        }.getOrNull()
    }
}
