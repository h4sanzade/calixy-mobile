package com.hasanzade.calixy_mobile.domain.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false
)