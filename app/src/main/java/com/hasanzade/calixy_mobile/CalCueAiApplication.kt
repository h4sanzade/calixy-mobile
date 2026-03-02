package com.hasanzade.calixy_mobile

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class CalCueAiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

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