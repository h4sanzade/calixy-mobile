package com.hasanzade.calixy_mobile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState

    private val _loginValidation = MutableStateFlow(LoginValidation())
    val loginValidation: StateFlow<LoginValidation> = _loginValidation

    private val _signUpValidation = MutableStateFlow(SignUpValidation())
    val signUpValidation: StateFlow<SignUpValidation> = _signUpValidation

    fun signIn(email: String, password: String) {
        if (validateLoginInput(email, password)) {
            viewModelScope.launch {
                authRepository.signInWithEmailAndPassword(email, password).collect {
                    _authState.value = it
                }
            }
        }
    }

    fun signUp(email: String, password: String, fullName: String) {
        if (validateSignUpInput(email, password, fullName)) {
            viewModelScope.launch {
                authRepository.signUpWithEmailAndPassword(email, password, fullName).collect {
                    _authState.value = it
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (isValidEmail(email)) {
            viewModelScope.launch {
                authRepository.sendPasswordResetEmail(email).collect {
                    _authState.value = it
                }
            }
        } else {
            _authState.value = AuthResult.Error("Please enter correct email")
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            authRepository.resendEmailVerification().collect {
                _authState.value = it
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            authRepository.checkEmailVerification().collect {
                _authState.value = it
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setFirstLaunchCompleted()
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        val emailError = if (email.isBlank()) "Email is required"
        else if (!isValidEmail(email)) "Please enter correct email"
        else null

        val passwordError = if (password.isBlank()) "Password is required"
        else if (password.length < 6) "Please enter correct password"
        else null

        _loginValidation.value = LoginValidation(emailError, passwordError)

        return emailError == null && passwordError == null
    }

    private fun validateSignUpInput(email: String, password: String, fullName: String): Boolean {
        val nameError = if (fullName.isBlank()) "Full name is required" else null
        val emailError = if (email.isBlank()) "Email is required"
        else if (!isValidEmail(email)) "Please enter correct email"
        else null
        val passwordError = if (password.isBlank()) "Password is required"
        else if (password.length < 6) "Password must be at least 6 characters"
        else null

        _signUpValidation.value = SignUpValidation(nameError, emailError, passwordError)

        return nameError == null && emailError == null && passwordError == null
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun resetAuthState() {
        _authState.value = null
    }

    fun clearValidationErrors() {
        _loginValidation.value = LoginValidation()
        _signUpValidation.value = SignUpValidation()
    }
}

data class LoginValidation(
    val emailError: String? = null,
    val passwordError: String? = null
)

data class SignUpValidation(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)