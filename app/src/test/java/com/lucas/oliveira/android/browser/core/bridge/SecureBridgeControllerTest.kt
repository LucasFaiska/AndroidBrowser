package com.lucas.oliveira.android.browser.core.bridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecureBridgeControllerTest {

    private lateinit var permissionStore: FakeBridgePermissionStore
    private lateinit var controller: SecureBridgeController

    private var dialogTriggered = false
    private var lastDialogOrigin: String? = null
    private var dialogCallback: ((PermissionDecision) -> Unit)? = null

    private var resolveTriggered = false
    private var lastResolveCallbackName: String? = null
    private var lastResolveData: String? = null

    @Before
    fun setup() {
        permissionStore = FakeBridgePermissionStore()
        dialogTriggered = false
        lastDialogOrigin = null
        dialogCallback = null
        resolveTriggered = false
        lastResolveCallbackName = null
        lastResolveData = null

        controller = SecureBridgeController(
            permissionStore = permissionStore,
            onShowDialog = { origin, callback ->
                dialogTriggered = true
                lastDialogOrigin = origin
                dialogCallback = callback
            },
            onResolveInteraction = { callbackName, data ->
                resolveTriggered = true
                lastResolveCallbackName = callbackName
                lastResolveData = data
            }
        )
    }

    @Test
    fun `handleInteraction with invalid url resolves with invalid origin error`() {
        controller.handleInteraction("invalid_url", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Invalid Origin", lastResolveData)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction when ALLOWED_ONCE resolves immediately`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ONCE)

        controller.handleInteraction("https://example.com/some/path", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertTrue(lastResolveData?.contains("appVersion") ?: false)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction when ALLOWED_ALWAYS resolves immediately`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        controller.handleInteraction("https://example.com/another/path", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertTrue(lastResolveData?.contains("appVersion") ?: false)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction when DENIED resolves with error immediately`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.DENIED)

        controller.handleInteraction("https://example.com/path", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Permission Denied", lastResolveData)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction when UNDETERMINED triggers dialog and resolves correctly when allowed`() {
        controller.handleInteraction("https://example.com/path?foo=bar", "myCallback")

        assertTrue(dialogTriggered)
        assertEquals("https://example.com", lastDialogOrigin)
        assertFalse(resolveTriggered)

        // Invoke callback with ALLOWED_ONCE
        dialogCallback?.invoke(PermissionDecision.ALLOWED_ONCE)

        assertEquals(PermissionDecision.ALLOWED_ONCE, permissionStore.getPermission("https://example.com"))
        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertTrue(lastResolveData?.contains("appVersion") ?: false)
    }

    @Test
    fun `handleInteraction when UNDETERMINED triggers dialog and resolves correctly when denied`() {
        controller.handleInteraction("https://example.com", "myCallback")

        assertTrue(dialogTriggered)
        assertEquals("https://example.com", lastDialogOrigin)
        assertFalse(resolveTriggered)

        // Invoke callback with DENIED
        dialogCallback?.invoke(PermissionDecision.DENIED)

        assertEquals(PermissionDecision.DENIED, permissionStore.getPermission("https://example.com"))
        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Permission Denied", lastResolveData)
    }

    @Test
    fun `handleInteraction with null url resolves with invalid origin error`() {
        controller.handleInteraction(null, "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Invalid Origin", lastResolveData)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with blank url resolves with invalid origin error`() {
        controller.handleInteraction("   ", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Invalid Origin", lastResolveData)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with url having explicit port extracts origin with port`() {
        controller.handleInteraction("http://localhost:8080/index.html", "myCallback")

        assertTrue(dialogTriggered)
        assertEquals("http://localhost:8080", lastDialogOrigin)
        assertFalse(resolveTriggered)
    }

    @Test
    fun `handleInteraction with malformed or incomplete URI resolves with invalid origin error`() {
        // e.g. URI without host
        controller.handleInteraction("https://", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Invalid Origin", lastResolveData)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with URISyntaxException in URL resolves with invalid origin error`() {
        controller.handleInteraction("https://example .com", "myCallback")

        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Invalid Origin", lastResolveData)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction when UNDETERMINED triggers dialog and resolves correctly when ALLOWED_ALWAYS`() {
        controller.handleInteraction("https://example.com/path", "myCallback")

        assertTrue(dialogTriggered)
        assertEquals("https://example.com", lastDialogOrigin)
        assertFalse(resolveTriggered)

        // Invoke callback with ALLOWED_ALWAYS
        dialogCallback?.invoke(PermissionDecision.ALLOWED_ALWAYS)

        assertEquals(PermissionDecision.ALLOWED_ALWAYS, permissionStore.getPermission("https://example.com"))
        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertTrue(lastResolveData?.contains("appVersion") ?: false)
    }

    @Test
    fun `handleInteraction when UNDETERMINED triggers dialog and resolves correctly when UNDETERMINED`() {
        controller.handleInteraction("https://example.com/path", "myCallback")

        assertTrue(dialogTriggered)
        assertEquals("https://example.com", lastDialogOrigin)
        assertFalse(resolveTriggered)

        // Invoke callback with UNDETERMINED
        dialogCallback?.invoke(PermissionDecision.UNDETERMINED)

        assertEquals(PermissionDecision.UNDETERMINED, permissionStore.getPermission("https://example.com"))
        assertTrue(resolveTriggered)
        assertEquals("myCallback", lastResolveCallbackName)
        assertEquals("Error: Permission Denied", lastResolveData)
    }

    @Test
    fun `handleInteraction with malicious callback name containing injection characters drops request`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        val maliciousCallback = "onDeviceInfoReceived'); alert('pwned'); //"
        controller.handleInteraction("https://example.com", maliciousCallback)

        assertFalse(resolveTriggered)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with callback name containing spaces drops request`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        controller.handleInteraction("https://example.com", "invalid callback name")

        assertFalse(resolveTriggered)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with empty or blank callback name drops request`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        controller.handleInteraction("https://example.com", "")
        controller.handleInteraction("https://example.com", "   ")

        assertFalse(resolveTriggered)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with callback name exceeding 64 chars drops request`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        val longCallback = "a".repeat(65)
        controller.handleInteraction("https://example.com", longCallback)

        assertFalse(resolveTriggered)
        assertFalse(dialogTriggered)
    }

    @Test
    fun `handleInteraction with valid callback containing alphanumeric, underscores and hyphens succeeds`() {
        permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        val validCallback = "valid_callback-Identifier_123"
        controller.handleInteraction("https://example.com", validCallback)

        assertTrue(resolveTriggered)
        assertEquals(validCallback, lastResolveCallbackName)
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
