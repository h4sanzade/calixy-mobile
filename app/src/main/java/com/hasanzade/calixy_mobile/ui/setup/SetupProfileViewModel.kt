package com.hasanzade.calixy_mobile.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hasanzade.calixy_mobile.domain.model.AuthResult
import com.hasanzade.calixy_mobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SetupProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var firstName: String = ""
    var lastName: String = ""
    var profileImageBase64: String = ""
    var gender: String = ""
    var dateOfBirth: String = ""
    var height: Double = 164.0
    var weight: Double = 70.0
    var activityLevel: String = "MODERATE"
    var goals: List<String> = emptyList()

    private val _setupState = MutableStateFlow<AuthResult?>(null)
    val setupState: StateFlow<AuthResult?> = _setupState

    fun submitProfile() {
        viewModelScope.launch {
            _setupState.value = AuthResult.Loading
            try {
                authRepository.userPreferences.setProfileSetupCompleted()
                _setupState.value = AuthResult.Success
            } catch (e: Exception) {
                _setupState.value = AuthResult.Error("An error occurred")
            }
        }
    }

    fun resetState() {
        _setupState.value = null
    }
}

class SetupProfileViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SetupProfileViewModel(authRepository) as T
    }
}