package com.hasanzade.calixy_mobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            delay(2000)

            val isFirstLaunch = userPreferences.isFirstLaunch.first()
            val isLoggedIn = userPreferences.isLoggedIn.first()

            when {
                isFirstLaunch -> _navigationState.value = SplashNavigationState.NavigateToOnboarding
                isLoggedIn -> _navigationState.value = SplashNavigationState.NavigateToMain
                else -> _navigationState.value = SplashNavigationState.NavigateToLogin
            }
        }
    }
}

sealed class SplashNavigationState {
    object Loading : SplashNavigationState()
    object NavigateToOnboarding : SplashNavigationState()
    object NavigateToLogin : SplashNavigationState()
    object NavigateToMain : SplashNavigationState()
}
