package com.lucas.oliveira.android.browser.core.bridge

interface BridgePermissionStore {
    fun getPermission(origin: String): PermissionDecision
    fun setPermission(origin: String, decision: PermissionDecision)
    fun clear()
}
