package com.lucas.oliveira.android.browser.core.bridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecureJavascriptBridgeTest {

    private lateinit var permissionStore: FakeBridgePermissionStore
    private lateinit var controller: SecureBridgeController
    private lateinit var bridge: SecureJavascriptBridge

    private var resolveTriggered = false
    private var lastResolveCallbackName: String? = null
    private var lastResolveData: String? = null

    @Before
    fun setup() {
        permissionStore = FakeBridgePermissionStore()
        resolveTriggered = false
        lastResolveCallbackName = null
        lastResolveData = null

        controller = SecureBridgeController(
            permissionStore = permissionStore,
            onShowDialog = { _, _ -> },
            onResolveInteraction = { callbackName, data ->
                resolveTriggered = true
                lastResolveCallbackName = callbackName
                lastResolveData = data
            }
        )

        bridge = SecureJavascriptBridge(controller) { "https://example.com" }
    }

    @Test
    fun `requestDeviceInfo passes the provider url and callback name to the controller`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        bridge.requestDeviceInfo("testCallback")

        assertTrue(resolveTriggered)
        assertEquals("testCallback", lastResolveCallbackName)
        assertTrue(lastResolveData?.contains("appVersion") ?: false)
    }

    private class FakeBridgePermissionStore : BridgePermissionStore {
        private val map = mutableMapOf<String, PermissionDecision>()
        override fun getPermission(origin: String): PermissionDecision = map[origin] ?: PermissionDecision.UNDETERMINED
        override fun setPermission(origin: String, decision: PermissionDecision) {
            map[origin] = decision
        }
        override fun clear() {
            map.clear()
        }
    }
}
