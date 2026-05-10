package com.lyecdevelopers.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun savePassword(password: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_PASSWORD, password).apply()
    }

    suspend fun getPassword(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_PASSWORD, null)
    }

    suspend fun clearPassword() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(KEY_PASSWORD).apply()
    }

    companion object {
        private const val KEY_PASSWORD = "encrypted_password"
    }
}
