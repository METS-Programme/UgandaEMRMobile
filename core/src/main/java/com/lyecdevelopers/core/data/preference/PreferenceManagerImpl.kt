package com.lyecdevelopers.core.data.preference

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lyecdevelopers.core.security.EncryptedPreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

class PreferenceManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptedPreferenceManager: EncryptedPreferenceManager
) : PreferenceManager {

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USERNAME = stringPreferencesKey("username")
        private val SELECTED_FORM_IDS = stringSetPreferencesKey("selected_form_ids")
        private val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        private val AUTO_SYNC_INTERVAL_HOURS = intPreferencesKey("auto_sync_interval_hours")
        private val SERVER_URL = stringPreferencesKey("server_url")
    }

    override suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_TOKEN] = token
        }
    }

    override fun getAuthToken(): Flow<String?> {
        return context.dataStore.data.map { it[AUTH_TOKEN] }
    }

    override suspend fun setIsLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs->
            prefs[IS_LOGGED_IN] = loggedIn
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map {
            it[IS_LOGGED_IN] ?: false
        }
    }

    override suspend fun saveUserRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ROLE] = role
        }
    }

    override fun getUserRole(): Flow<String?> {
        return context.dataStore.data.map { it[USER_ROLE] }
    }

    override suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME] = username
        }
    }

    override fun getUsername(): Flow<String?> =
        context.dataStore.data.map { it[USERNAME] }

    override suspend fun savePassword(password: String) {
        encryptedPreferenceManager.savePassword(password)
    }

    override fun getPassword(): Flow<String?> {
        return flow {
            emit(encryptedPreferenceManager.getPassword())
        }
    }

    override suspend fun setRememberMe(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REMEMBER_ME] = enabled
        }
    }

    override fun isRememberMeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[REMEMBER_ME] ?: false }
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    override fun isDarkModeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { it[DARK_MODE] ?: false }
    }

    override suspend fun saveSelectedForms(context: Context, ids: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_FORM_IDS] = ids
        }
    }

    override suspend fun loadSelectedForms(context: Context): Set<String> {
        val prefs = context.dataStore.data.first()
        return prefs[SELECTED_FORM_IDS] ?: emptySet()
    }

    override suspend fun saveAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_SYNC_ENABLED] = enabled
        }
    }

    override suspend fun loadAutoSyncEnabled(): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[AUTO_SYNC_ENABLED] ?: false
        }.first()
    }

    override suspend fun saveAutoSyncInterval(hours: Int) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_SYNC_INTERVAL_HOURS] = hours
        }
    }

    override suspend fun loadAutoSyncInterval(): Int {
        return context.dataStore.data.map { prefs ->
            prefs[AUTO_SYNC_INTERVAL_HOURS] ?: 12
        }.first()
    }


    override fun getServerUrl(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[SERVER_URL] ?: ""
        }
    }

    override suspend fun loadServerUrl(): String {
        return context.dataStore.data.map { prefs ->
            prefs[SERVER_URL]
        }.first() ?: ""
    }

    override suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL] = url
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }


}
