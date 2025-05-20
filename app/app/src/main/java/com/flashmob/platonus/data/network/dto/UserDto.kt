package com.flashmob.platonus.data.network.dto

import com.flashmob.platonus.data.model.UserRole

data class UserDto (
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val course: Int?
)