package com.flashmob.platonus.data.network.dto

data class LoginResponse (
    val token: String,
    val user: UserDto
)