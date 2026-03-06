package com.hasanzade.calixy_mobile.data.remote.model

data class SetupProfileRequest(
    val firstName: String,
    val lastName: String,
    val profileImage: String,
    val gender: String,
    val dateOfBirth: String,
    val height: Double,
    val weight: Double,
    val activityLevel: String,
    val goals: List<String>
)

data class SetupProfileResponse(
    val message: String?,
    val success: Boolean?
)