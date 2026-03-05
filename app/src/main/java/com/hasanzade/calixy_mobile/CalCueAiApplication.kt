package com.hasanzade.calixy_mobile

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hasanzade.calixy_mobile.data.local.UserPreferences
import com.hasanzade.calixy_mobile.data.remote.RetrofitClient
import com.hasanzade.calixy_mobile.domain.repository.AuthRepository

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class CalCueAiApplication : Application()

object AppModule {
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences(context.dataStore)
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(
            apiService = RetrofitClient.apiService,
            userPreferences = provideUserPreferences(context)
        )
    }
}