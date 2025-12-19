package com.projectapp.tempus

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSessionStore(context: Context) : SessionStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_auth_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveSession(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("expires_in", expiresIn)
            .apply()
    }

    override fun getAccessToken(): String? =
        prefs.getString("access_token", null)

    override fun getRefreshToken(): String? =
        prefs.getString("refresh_token", null)

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
