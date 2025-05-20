package com.flashmob.platonus.data.repository

import android.content.Context
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.data.network.ApiClient
import com.flashmob.platonus.data.network.dto.LoginRequest
import com.flashmob.platonus.data.network.dto.UserDto
import com.flashmob.platonus.data.storage.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    private val api = ApiClient.service
    private val authManager = AuthManager(context)

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val resp = api.login(LoginRequest(email, password))
            authManager.token = resp.token
            val user = resp.user.toDomain()
            authManager.user = user
            user
        }
    }

    private fun UserDto.toDomain() = User(id, name, email, role, course)
}