package com.hasanzade.calixy_mobile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    val userPreferences: UserPreferences
) {

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.register(
                RegisterRequest(firstName, lastName, email, password)
            )
            if (response.isSuccessful) {
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error(parseError(response.code())))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(networkError(e)))
        }
    }

    fun login(email: String, password: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                val accessToken = body?.accessToken ?: ""
                val refreshToken = body?.refreshToken ?: ""
                val user = body?.user
                val fullName = "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim()
                userPreferences.saveUserData(email, fullName, accessToken, refreshToken)
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error(parseError(response.code())))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(networkError(e)))
        }
    }

    fun verifyEmail(email: String, code: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.verifyEmail(VerifyEmailRequest(email, code))
            if (response.isSuccessful) {
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error("Wrong"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(networkError(e)))
        }
    }

    fun resendVerification(email: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.resendVerification(ResendVerificationRequest(email))
            if (response.isSuccessful) {
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error(parseError(response.code())))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(networkError(e)))
        }
    }

    fun forgotPassword(email: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error(parseError(response.code())))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(networkError(e)))
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
            if (response.isSuccessful) {
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error(parseError(response.code())))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(networkError(e)))
        }
    }

    suspend fun logout() {
        try {
            val accessToken = userPreferences.accessToken.first()
            val refreshToken = userPreferences.refreshToken.first()
            if (accessToken.isNotEmpty()) {
                apiService.logout("Bearer $accessToken", refreshToken)
            }
        } catch (e: Exception) {
            // local data-nı hər halda silirik
        } finally {
            userPreferences.clearUserData()
        }
    }

    private fun parseError(code: Int): String = when (code) {
        400 -> "Invalid request"
        401 -> "Email or password is incorrect"
        404 -> "User not found"
        409 -> "This email is already registered"
        422 -> "Please check your information"
        500 -> "Server error, please try again"
        else -> "An error occurred"
    }

    private fun networkError(e: Exception): String {
        return if (e.message?.contains("Unable to resolve host") == true ||
            e.message?.contains("timeout") == true
        ) "No internet connection" else "An error occurred"
    }
}