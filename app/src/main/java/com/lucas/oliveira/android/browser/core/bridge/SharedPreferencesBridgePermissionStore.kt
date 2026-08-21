package com.lucas.oliveira.android.browser.core.bridge

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SharedPreferencesBridgePermissionStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BridgePermissionStore {
    private val sharedPreferences = context.getSharedPreferences("secure_bridge_permissions", Context.MODE_PRIVATE)
    private val sessionPermissions = ConcurrentHashMap<String, PermissionDecision>()

    override fun getPermission(origin: String): PermissionDecision {
        val sessionDecision = sessionPermissions[origin]
        if (sessionDecision != null) {
            return sessionDecision
        }

        val savedValue = sharedPreferences.getString(origin, null) ?: return PermissionDecision.UNDETERMINED
        return runCatching {
            PermissionDecision.valueOf(savedValue)
        }.getOrDefault(PermissionDecision.UNDETERMINED)
    }

    override fun setPermission(origin: String, decision: PermissionDecision) {
        when (decision) {
            PermissionDecision.ALLOWED_ONCE -> {
                sessionPermissions[origin] = decision
                sharedPreferences.edit { remove(origin) }
            }
            PermissionDecision.ALLOWED_ALWAYS,
            PermissionDecision.DENIED -> {
                sessionPermissions.remove(origin)
                sharedPreferences.edit { putString(origin, decision.name) }
            }
            PermissionDecision.UNDETERMINED -> {
                sessionPermissions.remove(origin)
                sharedPreferences.edit { remove(origin) }
            }
        }
    }

    override fun clear() {
        sessionPermissions.clear()
        sharedPreferences.edit { clear() }
    }
}
