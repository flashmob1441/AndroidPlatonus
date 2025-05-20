package com.flashmob.platonus.data.model

data class User (
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val course: Int? = null
)