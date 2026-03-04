package com.hasanzade.calixy_mobile


data class RegisterRequest(val email: String, val password: String, val confirmPassword: String)
data class LoginRequest(val email: String, val password: String)
data class VerifyEmailRequest(val email: String, val code: String)
data class ResendVerificationRequest(val email: String)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)
data class RefreshTokenRequest(val refreshToken: String)
data class UpdateMeRequest(val firstName: String, val lastName: String, val phoneNumber: String? = null)


data class AuthResponse(val accessToken: String?, val refreshToken: String?, val user: UserResponse?, val message: String?)
data class UserResponse(val id: String?, val firstName: String?, val lastName: String?, val email: String?, val phoneNumber: String?)
data class BaseResponse(val message: String?, val success: Boolean?)
data class GoogleLoginRequest(val idToken: String)