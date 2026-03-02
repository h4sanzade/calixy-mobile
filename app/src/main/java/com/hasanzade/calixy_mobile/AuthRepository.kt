package com.hasanzade.calixy_mobile
import UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.hasanzade.calixy_mobile.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    val userPreferences: UserPreferences
) {

    fun signUpWithEmailAndPassword(email: String, password: String, fullName: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)

            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                user.sendEmailVerification().await()
                userPreferences.saveUserData(email, fullName)
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error("Registration failed"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(getErrorMessage(e)))
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                if (user.isEmailVerified) {
                    userPreferences.saveUserData(email, user.displayName ?: "")
                    emit(AuthResult.Success)
                } else {
                    emit(AuthResult.Error("Please verify your email first"))
                }
            } else {
                emit(AuthResult.Error("Login failed"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(getErrorMessage(e)))
        }
    }

    fun sendPasswordResetEmail(email: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(AuthResult.Success)
        } catch (e: Exception) {
            emit(AuthResult.Error(getErrorMessage(e)))
        }
    }

    fun confirmPasswordReset(email: String, newPassword: String): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            emit(AuthResult.Success)
        } catch (e: Exception) {
            emit(AuthResult.Error(getErrorMessage(e)))
        }
    }

    fun resendEmailVerification(): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                emit(AuthResult.Success)
            } else {
                emit(AuthResult.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(getErrorMessage(e)))
        }
    }

    fun checkEmailVerification(): Flow<AuthResult> = flow {
        try {
            emit(AuthResult.Loading)
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                if (user.isEmailVerified) {
                    emit(AuthResult.Success)
                } else {
                    emit(AuthResult.Error("Email not verified yet"))
                }
            } else {
                emit(AuthResult.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(getErrorMessage(e)))
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        userPreferences.clearUserData()
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("badly formatted") == true -> "Please enter correct email"
            exception.message?.contains("invalid") == true -> "Please enter correct password"
            exception.message?.contains("no user record") == true -> "Please enter correct email"
            exception.message?.contains("already in use") == true -> "This email is already registered"
            exception.message?.contains("too weak") == true -> "Password must be at least 6 characters"
            else -> exception.message ?: "An error occurred"
        }
    }
}