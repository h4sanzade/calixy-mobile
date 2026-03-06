package com.hasanzade.calixy_mobile.data.remote

import com.hasanzade.calixy_mobile.data.remote.model.AuthResponse
import com.hasanzade.calixy_mobile.data.remote.model.BaseResponse
import com.hasanzade.calixy_mobile.data.remote.model.ForgotPasswordRequest
import com.hasanzade.calixy_mobile.data.remote.model.GoogleLoginRequest
import com.hasanzade.calixy_mobile.data.remote.model.LoginRequest
import com.hasanzade.calixy_mobile.data.remote.model.RefreshTokenRequest
import com.hasanzade.calixy_mobile.data.remote.model.RegisterRequest
import com.hasanzade.calixy_mobile.data.remote.model.ResendVerificationRequest
import com.hasanzade.calixy_mobile.data.remote.model.ResetPasswordRequest
import com.hasanzade.calixy_mobile.data.remote.model.SetupProfileRequest
import com.hasanzade.calixy_mobile.data.remote.model.SetupProfileResponse
import com.hasanzade.calixy_mobile.data.remote.model.UpdateMeRequest
import com.hasanzade.calixy_mobile.data.remote.model.UserResponse
import com.hasanzade.calixy_mobile.data.remote.model.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

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

    @PUT("users/me/setup-profile")
    suspend fun setupProfile(
        @Header("Authorization") accessToken: String,
        @Body request: SetupProfileRequest
    ): Response<SetupProfileResponse>
}