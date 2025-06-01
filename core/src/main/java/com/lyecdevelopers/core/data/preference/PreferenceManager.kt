package com.lyecdevelopers.core.data.preference

import kotlinx.coroutines.flow.Flow

interface PreferenceManager {
    suspend fun saveAuthToken(token: String)
    fun getAuthToken(): Flow<String?>

    suspend fun setIsLoggedIn (loggedIn: Boolean)
    fun isLoggedIn() : Flow<Boolean>

    suspend fun saveUserRole(role: String)
    fun getUserRole(): Flow<String?>

    suspend fun setRememberMe(enabled: Boolean)
    fun isRememberMeEnabled(): Flow<Boolean>

    suspend fun setDarkModeEnabled(enabled: Boolean)
    fun isDarkModeEnabled(): Flow<Boolean>

    suspend fun clear()
}
