package com.hasanzade.calixy_mobile.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val IS_PROFILE_SETUP = booleanPreferencesKey("is_profile_setup")
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { it[IS_FIRST_LAUNCH] ?: true }
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val userEmail: Flow<String> = dataStore.data.map { it[USER_EMAIL] ?: "" }
    val userName: Flow<String> = dataStore.data.map { it[USER_NAME] ?: "" }
    val accessToken: Flow<String> = dataStore.data.map { it[ACCESS_TOKEN] ?: "" }
    val refreshToken: Flow<String> = dataStore.data.map { it[REFRESH_TOKEN] ?: "" }
    val isProfileSetup: Flow<Boolean> = dataStore.data.map { it[IS_PROFILE_SETUP] ?: false }

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { it[IS_FIRST_LAUNCH] = false }
    }

    suspend fun saveUserData(email: String, name: String, accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[USER_EMAIL] = email
            prefs[USER_NAME] = name
            prefs[IS_LOGGED_IN] = true
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun setProfileSetupCompleted() {
        dataStore.edit { it[IS_PROFILE_SETUP] = true }
    }

    suspend fun clearUserData() {
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[IS_FIRST_LAUNCH] = false
        }
    }
}