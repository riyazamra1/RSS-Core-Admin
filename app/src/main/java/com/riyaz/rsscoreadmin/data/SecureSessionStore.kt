package com.riyaz.rsscoreadmin.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureSessionStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        "rss_admin_session",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun saveAccessToken(token: String) { prefs.edit().putString("access_token", token).apply() }
    fun clear() { prefs.edit().clear().apply() }
}