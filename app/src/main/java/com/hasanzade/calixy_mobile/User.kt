package com.hasanzade.calixy_mobile

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false
)