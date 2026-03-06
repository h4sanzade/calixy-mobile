package com.hasanzade.calixy_mobile.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hasanzade.calixy_mobile.data.remote.model.SetupProfileRequest
import com.hasanzade.calixy_mobile.domain.model.AuthResult
import com.hasanzade.calixy_mobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
                val accessToken = authRepository.userPreferences.accessToken.first()
                val request = SetupProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    profileImage = profileImageBase64,
                    gender = gender,
                    dateOfBirth = dateOfBirth,
                    height = height,
                    weight = weight,
                    activityLevel = activityLevel,
                    goals = goals
                )
                val response = authRepository.setupProfile("Bearer $accessToken", request)
                if (response.isSuccessful) {
                    _setupState.value = AuthResult.Success
                } else {
                    _setupState.value = AuthResult.Error(
                        when (response.code()) {
                            400 -> "Invalid data"
                            401 -> "Session expired, please login again"
                            500 -> "Server error, please try again"
                            else -> "An error occurred"
                        }
                    )
                }
            } catch (e: Exception) {
                _setupState.value = AuthResult.Error(
                    if (e.message?.contains("Unable to resolve host") == true ||
                        e.message?.contains("timeout") == true
                    ) "No internet connection" else "An error occurred"
                )
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