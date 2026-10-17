package com.riyaz.rsscoreadmin.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Keeps the administrator token in encrypted preferences.
 *
 * Android Keystore state can become invalid after device restore, OS updates, or
 * app-data migration. Do not let that exceptional state crash MainActivity before
 * the login screen is rendered. If the encrypted store cannot be opened, the app
 * starts signed out and token persistence remains unavailable rather than storing
 * credentials in plaintext.
 */
class SecureSessionStore(context: Context) {
    private val appContext = context.applicationContext

    private val prefs: SharedPreferences? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        openEncryptedPreferences()
    }

    private fun openEncryptedPreferences(): SharedPreferences? {
        fun create(): SharedPreferences = EncryptedSharedPreferences.create(
            "rss_admin_session",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return try {
            create()
        } catch (_: Exception) {
            // A damaged or stale encrypted preference file can prevent startup.
            // Clear only this app's session file, then retry once. Never use
            // plaintext preferences for authentication tokens.
            try {
                appContext.getSharedPreferences("rss_admin_session", Context.MODE_PRIVATE)
                    .edit().clear().commit()
                create()
            } catch (_: Exception) {
                null
            }
        }
    }

    fun getAccessToken(): String? = runCatching { prefs?.getString("access_token", null) }.getOrNull()

    fun saveAccessToken(token: String) {
        val store = prefs ?: error("Secure session storage is unavailable. Clear app data and sign in again.")
        check(store.edit().putString("access_token", token).commit()) {
            "Could not save the secure session. Please sign in again."
        }
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
