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
                authRepository.login(email, password).collect {
                    _authState.value = it
                }
            }
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (validateSignUpInput(email, password, confirmPassword)) {
            viewModelScope.launch {
                authRepository.register(email, password, confirmPassword).collect {
                    _authState.value = it
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.googleLogin(idToken).collect {
                _authState.value = it
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (isValidEmail(email)) {
            viewModelScope.launch {
                authRepository.forgotPassword(email).collect {
                    _authState.value = it
                }
            }
        } else {
            _authState.value = AuthResult.Error("Please enter correct email")
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

    private fun validateSignUpInput(
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val emailError = if (email.isBlank()) "Email is required"
        else if (!isValidEmail(email)) "Please enter correct email"
        else null
        val passwordError = if (password.isBlank()) "Password is required"
        else if (password.length < 6) "Password must be at least 6 characters"
        else null
        val confirmPasswordError = if (confirmPassword.isBlank()) "Confirm password is required"
        else if (password != confirmPassword) "Passwords don't match"
        else null
        _signUpValidation.value = SignUpValidation(emailError, passwordError, confirmPasswordError)
        return emailError == null && passwordError == null && confirmPasswordError == null
    }

    private fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun resetAuthState() { _authState.value = null }

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
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)