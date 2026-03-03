package com.hasanzade.calixy_mobile

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiService {

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<BaseResponse>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<BaseResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<BaseResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") accessToken: String,
        @Header("Refresh-Token") refreshToken: String
    ): Response<BaseResponse>

    @PATCH("auth/me")
    suspend fun updateMe(
        @Header("Authorization") accessToken: String,
        @Body request: UpdateMeRequest
    ): Response<UserResponse>
}