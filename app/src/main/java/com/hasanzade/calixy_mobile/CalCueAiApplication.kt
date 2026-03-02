package com.hasanzade.calixy_mobile

import AuthRepository
import UserPreferences
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class CalCueAiApplication : Application() {

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

// DI Extensions for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Firebase Extensions
object FirebaseModule {
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences(context.dataStore)
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(
            firebaseAuth = provideFirebaseAuth(),
            userPreferences = provideUserPreferences(context)
        )
    }
}