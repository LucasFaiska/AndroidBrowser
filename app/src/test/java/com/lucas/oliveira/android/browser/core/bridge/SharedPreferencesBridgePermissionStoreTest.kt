package com.lucas.oliveira.android.browser.core.bridge

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SharedPreferencesBridgePermissionStoreTest {

    private lateinit var context: Context
    private lateinit var store: SharedPreferencesBridgePermissionStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val sharedPrefs = context.getSharedPreferences("secure_bridge_permissions", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().commit()
        store = SharedPreferencesBridgePermissionStore(context)
    }

    @Test
    fun `getPermission returns UNDETERMINED by default`() {
        assertEquals(PermissionDecision.UNDETERMINED, store.getPermission("https://example.com"))
    }

    @Test
    fun `ALLOWED_ONCE is saved in memory only and not in shared preferences`() {
        store.setPermission("https://example.com", PermissionDecision.ALLOWED_ONCE)
        
        // Retrieve using the same store instance -> should be ALLOWED_ONCE
        assertEquals(PermissionDecision.ALLOWED_ONCE, store.getPermission("https://example.com"))

        // Create a new store instance with the same underlying preferences -> should be UNDETERMINED
        val secondStore = SharedPreferencesBridgePermissionStore(context)
        assertEquals(PermissionDecision.UNDETERMINED, secondStore.getPermission("https://example.com"))
    }

    @Test
    fun `ALLOWED_ALWAYS is persisted in shared preferences`() {
        store.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

        // Same instance
        assertEquals(PermissionDecision.ALLOWED_ALWAYS, store.getPermission("https://example.com"))

        // New instance
        val secondStore = SharedPreferencesBridgePermissionStore(context)
        assertEquals(PermissionDecision.ALLOWED_ALWAYS, secondStore.getPermission("https://example.com"))
    }

    @Test
    fun `DENIED is persisted in shared preferences`() {
        store.setPermission("https://example.com", PermissionDecision.DENIED)

        // Same instance
        assertEquals(PermissionDecision.DENIED, store.getPermission("https://example.com"))

        // New instance
        val secondStore = SharedPreferencesBridgePermissionStore(context)
        assertEquals(PermissionDecision.DENIED, secondStore.getPermission("https://example.com"))
    }

    @Test
    fun `clear removes both session and persistent permissions`() {
        store.setPermission("https://session.com", PermissionDecision.ALLOWED_ONCE)
        store.setPermission("https://persistent.com", PermissionDecision.ALLOWED_ALWAYS)

        store.clear()

        assertEquals(PermissionDecision.UNDETERMINED, store.getPermission("https://session.com"))
        assertEquals(PermissionDecision.UNDETERMINED, store.getPermission("https://persistent.com"))
    }

    @Test
    fun `getPermission returns UNDETERMINED if stored value in shared preferences is invalid`() {
        // Manually write an invalid permission decision name to shared preferences
        val sharedPrefs = context.getSharedPreferences("secure_bridge_permissions", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("https://invalid-value.com", "INVALID_ENUM_VALUE").commit()

        // Create store and check permission
        val currentStore = SharedPreferencesBridgePermissionStore(context)
        assertEquals(PermissionDecision.UNDETERMINED, currentStore.getPermission("https://invalid-value.com"))
    }

    @Test
    fun `setPermission with UNDETERMINED removes both session and persistent permissions`() {
        // First set ALLOWED_ONCE (in memory) and verify it's saved
        store.setPermission("https://example-session.com", PermissionDecision.ALLOWED_ONCE)
        assertEquals(PermissionDecision.ALLOWED_ONCE, store.getPermission("https://example-session.com"))

        // Set to UNDETERMINED
        store.setPermission("https://example-session.com", PermissionDecision.UNDETERMINED)
        assertEquals(PermissionDecision.UNDETERMINED, store.getPermission("https://example-session.com"))

        // Now set ALLOWED_ALWAYS (persistent) and verify it's saved
        store.setPermission("https://example-persistent.com", PermissionDecision.ALLOWED_ALWAYS)
        assertEquals(PermissionDecision.ALLOWED_ALWAYS, store.getPermission("https://example-persistent.com"))

        // Set to UNDETERMINED
        store.setPermission("https://example-persistent.com", PermissionDecision.UNDETERMINED)
        assertEquals(PermissionDecision.UNDETERMINED, store.getPermission("https://example-persistent.com"))

        // Verify with a new store instance that the persistent value was removed
        val secondStore = SharedPreferencesBridgePermissionStore(context)
        assertEquals(PermissionDecision.UNDETERMINED, secondStore.getPermission("https://example-persistent.com"))
    }
}
