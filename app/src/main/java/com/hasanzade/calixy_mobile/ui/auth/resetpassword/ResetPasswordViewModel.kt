package com.hasanzade.calixy_mobile.ui.auth.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasanzade.calixy_mobile.domain.repository.AuthRepository
import com.hasanzade.calixy_mobile.domain.model.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _resetPasswordState = MutableStateFlow<AuthResult?>(null)
    val resetPasswordState: StateFlow<AuthResult?> = _resetPasswordState

    private val _passwordValidation = MutableStateFlow(PasswordValidation())
    val passwordValidation: StateFlow<PasswordValidation> = _passwordValidation

    fun resetPassword(email: String, code: String, newPassword: String, confirmPassword: String) {
        if (validatePasswords(newPassword, confirmPassword)) {
            viewModelScope.launch {
                authRepository.resetPassword(email, code, newPassword).collect {
                    _resetPasswordState.value = it
                }
            }
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        val newPasswordError = when {
            newPassword.isBlank() -> "Password is required"
            newPassword.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
        val confirmPasswordError = when {
            confirmPassword.isBlank() -> "Confirm password is required"
            newPassword != confirmPassword -> "Passwords don't match"
            else -> null
        }
        _passwordValidation.value = PasswordValidation(newPasswordError, confirmPasswordError)
        return newPasswordError == null && confirmPasswordError == null
    }

    fun clearValidationErrors() { _passwordValidation.value = PasswordValidation() }
    fun resetState() { _resetPasswordState.value = null }
}

data class PasswordValidation(
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null
)